package planning;

public class HTNNode {
    /*
    private static final Random RND = new Random(1);

    private Level level;

    private int g;
    private HTNNode parent;
    public PrimitiveTask action;

    private Agent agent;

    private HTNNode ChildNode() {
        HTNNode copy = new HTNNode(this);
        System.arraycopy(this.agents, 0, copy.getAgents(), 0, this.agents.size());
        copy.setBoxes(new HashMap<>(this.boxes));
        return copy;
    }

    public HTNNode(HTNNode node) {
        this(node.getLevel(), node);
    }

    public HTNNode(Level level) {
        this(level, null);
    }

    public HTNNode(Level level, HTNNode node) {
        this.boxes = new HashMap<Coordinate, Box>();
        this.agents = new ArrayList<Agent>();

        this.parent = parent;
        if (parent == null) {
            this.g = 0;
        } else {
            this.g = parent.getG() + 1;
        }

        this.level = level;
        this.agent = null;
    }

    public Level getLevel() {
        return this.level;
    }

    public int getG() {
        return this.g;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void computeGoalDistance() throws NotImplementedException {
        for (Goal goal : this.level.getGoals()) {
            throw new NotImplementedException();
            // TODO: to be implemented
        }
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public LinkedList<HTNNode> extractPlan() {
        LinkedList<HTNNode> plan = new LinkedList<HTNNode>();
        HTNNode n = this;
        while (!n.isInitialState()) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    public ArrayList<HTNNode> getExpandedNodes() {
        ArrayList<HTNNode> expandedNodes = new ArrayList<HTNNode>(PrimitiveTask.every.length);
        for (PrimitiveTask c : PrimitiveTask.every) {
            // Determine applicability of action
            int newAgentRow = this.agent.getX() + PrimitiveTask.dirToRowChange(c.dir1);
            int newAgentCol = this.agent.getY() + PrimitiveTask.dirToColChange(c.dir1);

            if (c.actionType == type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    HTNNode n = this.ChildNode();
                    n.action = c;
                    n.agent.setCoordinate(newAgentRow, newAgentCol);
                    expandedNodes.add(n);
                }
            } else if (c.actionType == type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + PrimitiveTask.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + PrimitiveTask.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        HTNNode n = this.ChildNode();
                        n.action = c;
                        n.agent.setCoordinate(newAgentRow, newAgentCol);
                        Box box = n.boxes.remove(new Coordinate(newAgentRow, newAgentCol));
                        box.setCoordinate(newBoxRow, newBoxCol);
                        n.boxes.put(new Coordinate(newBoxRow, newBoxCol), box);
                        expandedNodes.add(n);
                    }
                }
            } else if (c.actionType == type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = this.agent.getX() + PrimitiveTask.dirToRowChange(c.dir2);
                    int boxCol = this.agent.getY() + PrimitiveTask.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        HTNNode n = this.ChildNode();
                        n.action = c;
                        n.agent.setCoordinate(newAgentRow, newAgentCol);
                        Box box = n.boxes.remove(new Coordinate(boxRow, boxCol));
                        box.setCoordinate(this.agent.getX(), this.agent.getY());
                        n.boxes.put(new Coordinate(this.agent.getX(), this.agent.getY()), box);
                        expandedNodes.add(n);
                    }
                }
            }
        }
        Collections.shuffle(expandedNodes, RND);
        return expandedNodes;
    }
    */
}
