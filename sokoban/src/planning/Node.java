package planning;

public class Node {
    /*
    private static final Random RND = new Random(1);

    private Level level;

    private int g;
    private Node parent;
    public Command action;

    private Agent agent;

    private Node ChildNode() {
        Node copy = new Node(this);
        System.arraycopy(this.agents, 0, copy.getAgents(), 0, this.agents.size());
        copy.setBoxes(new HashMap<>(this.boxes));
        return copy;
    }

    public Node(Node node) {
        this(node.getLevel(), node);
    }

    public Node(Level level) {
        this(level, null);
    }

    public Node(Level level, Node node) {
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

    public LinkedList<Node> extractPlan() {
        LinkedList<Node> plan = new LinkedList<Node>();
        Node n = this;
        while (!n.isInitialState()) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    public boolean isGoalState() {
        for (Box box : this.level.getBoxes()) {
            for (Goal goal : Level.getGoals()) {
                Coordinate boxPosition = box.getCoordinate();
                Coordinate goalPosition = goal.getCoordinate();
                if (goalPosition.getRow() == boxPosition.getRow() && goalPosition.getCol() == boxPosition.getCol()
                        && Character.toLowerCase(box.getBoxType()) != goal.getGoalType()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean cellIsFree(int row, int col) {
        Coordinate coordinate = new Coordinate(row, col);
        return this.level.isCellEmpty(coordinate);
    }

    private boolean boxAt(int row, int col) {
        Coordinate coordinate = new Coordinate(row, col);
        Box box = this.boxes.get(coordinate);
        return box != null;
    }

    public ArrayList<Node> getExpandedNodes() {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
        for (Command c : Command.every) {
            // Determine applicability of action
            int newAgentRow = this.agent.getX() + Command.dirToRowChange(c.dir1);
            int newAgentCol = this.agent.getY() + Command.dirToColChange(c.dir1);

            if (c.actType == type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    Node n = this.ChildNode();
                    n.action = c;
                    n.agent.setCoordinate(newAgentRow, newAgentCol);
                    expandedNodes.add(n);
                }
            } else if (c.actType == type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        Node n = this.ChildNode();
                        n.action = c;
                        n.agent.setCoordinate(newAgentRow, newAgentCol);
                        Box box = n.boxes.remove(new Coordinate(newAgentRow, newAgentCol));
                        box.setCoordinate(newBoxRow, newBoxCol);
                        n.boxes.put(new Coordinate(newBoxRow, newBoxCol), box);
                        expandedNodes.add(n);
                    }
                }
            } else if (c.actType == type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = this.agent.getX() + Command.dirToRowChange(c.dir2);
                    int boxCol = this.agent.getY() + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        Node n = this.ChildNode();
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
