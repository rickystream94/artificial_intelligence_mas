package architecture.bdi;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.search.Node;
import architecture.search.Search;
import board.*;
import exceptions.NoAvailableBoxesException;
import exceptions.NoAvailableGoalsException;
import exceptions.NotLowestPriorityGoalException;
import logging.ConsoleLogger;
import utils.HashMapHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BDIManager {

    private static final Logger LOGGER = ConsoleLogger.getLogger(BDIManager.class.getSimpleName());

    private Set<Goal> unsolvedGoals;
    private Set<Desire> achievedGoalDesires;
    private Map<Agent, Desire> agentDesireMap;
    private LevelManager levelManager;
    private Queue<Goal> globalGoalPriorities;
    private Map<Agent, Set<Box>> agentBoxesMap;
    private Map<Agent, Set<Goal>> agentGoalsMap;
    private Map<Box, Set<Goal>> boxGoalsMap;

    public BDIManager() {
        this.unsolvedGoals = ConcurrentHashMap.newKeySet();
        this.unsolvedGoals.addAll(Level.getGoals());
        this.achievedGoalDesires = ConcurrentHashMap.newKeySet();
        this.agentDesireMap = new ConcurrentHashMap<>();
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.globalGoalPriorities = new PriorityQueue<>(new GoalComparator());
        preProcessing();
        computeGoalPriorities();
    }

    private void preProcessing() {
        List<Agent> allAgents = this.levelManager.getLevel().getAgents();
        List<Box> allBoxes = this.levelManager.getLevel().getBoxes();
        List<Goal> allGoals = Level.getGoals();

        // Compute Agent -> Movable Boxes mapping
        this.agentBoxesMap = new ConcurrentHashMap<>();
        for (Agent agent : allAgents) {
            Set<Box> movableBoxesForAgent = allBoxes.stream()
                    .filter(box -> box.getColor() == agent.getColor()).collect(Collectors.toSet());
            this.agentBoxesMap.put(agent, movableBoxesForAgent);
        }

        // Compute Agent -> Solvable Goals mapping
        this.agentGoalsMap = new ConcurrentHashMap<>();
        for (Agent agent : allAgents) {
            Set<Goal> solvableGoals = allGoals.stream().filter(goal -> this.agentBoxesMap.get(agent).stream().anyMatch(box -> Character.toLowerCase(box.getBoxType()) == goal.getGoalType())).collect(Collectors.toSet());
            this.agentGoalsMap.put(agent, solvableGoals);
        }

        // Compute Box -> Solvable Goals mapping
        this.boxGoalsMap = new ConcurrentHashMap<>();
        for (Box box : allBoxes) {
            Set<Goal> solvableGoals = allGoals.stream().filter(goal -> Character.toLowerCase(box.getBoxType()) == goal.getGoalType()).collect(Collectors.toSet());
            this.boxGoalsMap.put(box, solvableGoals);
        }
    }

    private void computeGoalPriorities() {
        // For each goal, start the A* search from the empty cell to the goal
        // Then count how many goals I have to pass through and use -(this number) as priority
        for (Goal goal : Level.getGoals()) {
            ConsoleLogger.logInfo(LOGGER, String.format("Computing priority for goal %s...", goal));
            // Start position for the search is the closest empty cell
            Set<Coordinate> emptyCells = levelManager.getLevel().getEmptyCellsPositions().stream().map(Coordinate::new).collect(Collectors.toSet());
            emptyCells.removeAll(Level.getGoals().stream().map(g -> new Coordinate(g.getCoordinate())).collect(Collectors.toSet()));
            emptyCells.addAll(levelManager.getLevel().getAgents().stream().map(a -> new Coordinate(a.getCoordinate())).collect(Collectors.toSet()));
            Map<Object, Integer> distances = new HashMap<>();
            emptyCells.forEach(c -> distances.put(c, Coordinate.manhattanDistance(goal.getCoordinate(), c)));
            Coordinate startPosition = (Coordinate) HashMapHelper.getKeyByMinIntValue(distances);
            Search search = new Search(startPosition, goal);
            Deque<Node> plan = search.search();
            int priority = countTraversedGoals(plan);
            List<Coordinate> neighbours = goal.getCoordinate().getClockwiseNeighbours();
            int walls = 0;
            for (Coordinate c : neighbours) {
                if (!Level.isNotWall(c))
                    walls++;
            }
            if (walls == 3)
                priority += 100;
            goal.setPriority(priority);
            this.globalGoalPriorities.add(goal);
            ConsoleLogger.logInfo(LOGGER, String.format("Priority: %d ", priority));
        }
    }

    private int countTraversedGoals(Deque<Node> plan) {
        int traversedGoals = 0;
        while (!plan.isEmpty()) {
            Node n = plan.pop();
            if (Level.isGoalCell(n.getPosition()))
                traversedGoals++;
        }
        return traversedGoals;
    }

    private static int getCostBetweenObjects(SokobanObject o1, SokobanObject o2) {
        return Coordinate.manhattanDistance(o1.getCoordinate(), o2.getCoordinate());
    }

    public synchronized Desire getNextGoalDesireForAgent(Agent agent) throws NoAvailableBoxesException, NotLowestPriorityGoalException, NoAvailableGoalsException {
        // Get all the goals that can be solved by this agent (that are not solved yet)
        Set<Goal> solvableGoals = getUnsolvedGoalsForAgent(agent);

        // Copy goal priorities into temp array (to preserve the priority queue)
        Goal[] goalsByPriority = this.globalGoalPriorities.toArray(new Goal[0]);
        Arrays.sort(goalsByPriority, new GoalComparator());

        // First iteration: get the priority of the first goal the agent could solve
        List<Goal> unsolvedGoalsWithLowerPriority = new ArrayList<>();
        int goalPriorityThreshold = 0;
        for (Goal goal : goalsByPriority) {
            if (solvableGoals.contains(goal)) {
                goalPriorityThreshold = goal.getPriority();
                break;
            }
            if (this.unsolvedGoals.contains(goal))
                unsolvedGoalsWithLowerPriority.add(goal);
        }
        final int finalThreshold = goalPriorityThreshold;

        // Second iteration: get all the goals with priority equal to the one found above
        List<Goal> goalsForAgentWithSamePriority = new ArrayList<>();
        Arrays.stream(goalsByPriority)
                .filter(goal -> goal.getPriority() == finalThreshold && solvableGoals.contains(goal))
                .forEach(goalsForAgentWithSamePriority::add);

        // Compute cost to achieve goal and choose the best one
        // Prefer goals at edge cells and close to solved goals and closest to the agent
        Map<Object, Integer> goalDistanceMap = new HashMap<>();
        goalsForAgentWithSamePriority.stream()
                .filter(goal -> !this.levelManager.getLevel().isGoalSolved(goal))
                .forEach(goal -> {
                    int cost = 0;
                    cost += getCostBetweenObjects(agent, goal);
                    if (Coordinate.isEdgeCell(goal.getCoordinate(), false))
                        cost -= 100;
                    List<Coordinate> neighbours = goal.getCoordinate().getClockwiseNeighbours();
                    for (Coordinate c : neighbours) {
                        if (!Level.isNotWall(c))
                            cost -= 100;
                        Goal neighbourGoal = Level.goalAt(c);
                        if (neighbourGoal != null && this.levelManager.getLevel().isGoalSolved(neighbourGoal))
                            cost -= 100;
                    }
                    goalDistanceMap.put(goal, cost);
                });
        Goal chosenGoal = (Goal) (HashMapHelper.getKeyByMinIntValue(goalDistanceMap));
        if (chosenGoal == null)
            throw new NoAvailableGoalsException(agent);

        // Get all boxes that satisfy the following requirements:
        // 1) Can be moved by the agent;
        // 2) Have not been assigned to another agent yet
        // 3) Are not currently solving a goal
        // 4) Closest box that can achieve the goal chosen above
        Set<Box> movableBoxes = this.agentBoxesMap.get(agent).stream() // (1)
                .filter(box -> this.agentDesireMap.values().stream().noneMatch(d -> d.getBox() != null && d.getBox().equals(box)) && // (2)
                        this.achievedGoalDesires.stream().noneMatch(desire -> desire.getBox().equals(box)) && // (3)
                        Character.toLowerCase(box.getBoxType()) == chosenGoal.getGoalType()) // (4.1)
                .collect(Collectors.toSet());
        if (movableBoxes.size() == 0)
            throw new NoAvailableBoxesException(agent); // No available boxes for the agent, will wait and send NoOps
        Map<Object, Integer> boxDistanceMap = new HashMap<>();
        movableBoxes.forEach(box -> boxDistanceMap.put(box, getCostBetweenObjects(agent, box)));
        Box chosenBox = (Box) (HashMapHelper.getKeyByMinIntValue(boxDistanceMap));
        assert chosenBox != null;

        // If there are unsolved goals with lower priority, agent has to wait until they're solved
        if (unsolvedGoalsWithLowerPriority.size() > 0) {
            throw new NotLowestPriorityGoalException(agent, chosenGoal);
        }
        return new GoalDesire(chosenBox, chosenGoal);
    }

    public void solvedGoal(Agent agent, Desire desire) {
        Goal goal = Level.goalAt(desire.getTarget());
        this.unsolvedGoals.remove(goal);
        this.achievedGoalDesires.add(desire);
        if (this.agentDesireMap.containsKey(agent) && this.agentDesireMap.get(agent).equals(desire))
            resetAgentDesire(agent);
    }

    public void agentCommitsToDesire(Agent agent, Desire desire) {
        this.agentDesireMap.put(agent, desire);
    }

    public void resetAgentDesire(Agent agent) {
        this.agentDesireMap.remove(agent);
    }

    public synchronized void checkIfSolvedGoalsAreStillSolved() {
        Iterator<Desire> it = this.achievedGoalDesires.iterator();
        while (it.hasNext()) {
            Desire desire = it.next();
            Goal goal = Level.goalAt(desire.getTarget());
            if (!this.levelManager.getLevel().isGoalSolved(goal)) {
                ConsoleLogger.logInfo(LOGGER, String.format("%s has to be achieved again!", goal));
                unsolvedGoals.add(goal);
                it.remove();
            }
        }
    }

    public synchronized boolean canSolveGoalWithNoPriorityConflicts(Box box) {
        Set<Goal> solvableGoals = getUnsolvedGoalsForBox(box);
        Goal[] goalsByPriority = this.globalGoalPriorities.toArray(new Goal[0]);
        Arrays.sort(goalsByPriority, new GoalComparator());

        // If there is at least one unsolved goal that has lower priority, this box can't be moved to its goal
        int goalsWithLowerPriority = 0;
        for (Goal goal : goalsByPriority) {
            if (solvableGoals.contains(goal))
                break;
            if (unsolvedGoals.contains(goal))
                goalsWithLowerPriority++;
        }
        return goalsWithLowerPriority == 0;
    }

    public Set<Goal> getUnsolvedGoalsForAgent(Agent agent) {
        return this.agentGoalsMap.get(agent).stream().filter(goal -> unsolvedGoals.contains(goal)).collect(Collectors.toSet());
    }

    public Set<Goal> getUnsolvedGoalsForBox(Box box) {
        return this.boxGoalsMap.get(box).stream().filter(goal -> unsolvedGoals.contains(goal)).collect(Collectors.toSet());
    }
}
