import java.util.*;

public class AStar {

    public static void main(String[] args) {
        int rows = 15;
        int cols = 15;
        double blockChance = 0.1;

        Node[][] world = generateWorld(rows, cols, blockChance);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Initial world:");
        displayWorld(world, null, null, null);

        while (true) {
            try {
                System.out.println("Enter starting node (row col), or type 'exit' to quit:");
                String input = scanner.nextLine();
                if (input.trim().equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    break;
                }

                int[] startCoords = parseCoordinates(input, rows, cols);
                Node startNode = world[startCoords[0]][startCoords[1]];

                System.out.println("Enter goal node (row col):");
                int[] goalCoords = parseCoordinates(scanner.nextLine(), rows, cols);
                Node goalNode = world[goalCoords[0]][goalCoords[1]];

                if (startNode.getType() == 1 || goalNode.getType() == 1) {
                    System.out.println("Start or goal node is blocked.");
                    continue;
                }

                List<Node> path = findPath(world, startNode, goalNode);
                displayWorld(world, startNode, goalNode, path);

                if (path != null) {
                    System.out.println("Path found:");
                    path.forEach(node -> System.out.println(node));
                } else {
                    System.out.println("No path found.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred. Please try again. Error: " + e.toString());
            }
        }
        scanner.close();
    }

    private static int[] parseCoordinates(String input, int maxRows, int maxCols) throws IllegalArgumentException {
        String[] parts = input.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected two integers for row and col.");
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            if (row < 0 || col < 0 || row >= maxRows || col >= maxCols) {
                throw new IllegalArgumentException("Coordinates out of bounds (" + row + ", " + col + "). Valid indices are 0 to " + (maxRows-1) + " for rows and 0 to " + (maxCols-1) + " for cols.");
            }
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Input must be integers.");
        }
    }


    private static Node[][] generateWorld(int rows, int cols, double blockChance) {
        Node[][] world = new Node[rows][cols];
        Random random = new Random();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                world[row][col] = new Node(row, col, random.nextDouble() < blockChance ? 1 : 0);
            }
        }

        return world;
    }

    private static void displayWorld(Node[][] world, Node start, Node goal, List<Node> path) {
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[row].length; col++) {
                Node node = world[row][col];
                if (node.equals(start)) {
                    System.out.print("S ");  // Start node
                } else if (node.equals(goal)) {
                    System.out.print("G ");  // Goal node
                } else if (path != null && path.contains(node)) {
                    System.out.print("* ");  // Path node
                } else {
                    System.out.print(node.getType() == 0 ? ". " : "# ");  // Free node or blocked node
                }
            }
            System.out.println();
        }
    }


    private static List<Node> findPath(Node[][] world, Node start, Node goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();

        start.setG(0);
        start.setH(manhattanDistance(start, goal));
        start.setF();

        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            closedSet.add(current);

            for (Node neighbor : getNeighbors(world, current)) {
                if (closedSet.contains(neighbor) || neighbor.getType() == 1) {
                    continue;
                }

                int tentativeG = current.getG() + 1;

                if (!openSet.contains(neighbor) || tentativeG < neighbor.getG()) {
                    cameFrom.put(neighbor, current);
                    neighbor.setG(tentativeG);
                    neighbor.setH(manhattanDistance(neighbor, goal));
                    neighbor.setF();

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // No path found
    }

    private static int manhattanDistance(Node node1, Node node2) {
        return Math.abs(node1.getRow() - node2.getRow()) + Math.abs(node1.getCol() - node2.getCol());
    }

    private static List<Node> getNeighbors(Node[][] world, Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newRow = node.getRow() + dr[i];
            int newCol = node.getCol() + dc[i];

            if (newRow >= 0 && newRow < world.length && newCol >= 0 && newCol < world[0].length) {
                neighbors.add(world[newRow][newCol]);
            }
        }

        return neighbors;
    }

    private static List<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<Node> path = new ArrayList<>();
        Node temp = current;
        while (temp != null) {
            path.add(temp);
            temp = cameFrom.get(temp);
        }
        Collections.reverse(path);
        return path;
    }
}

class Node {
    private int row, col, f, g, h, type;
    private Node parent;

    public Node(int r, int c, int t) {
        row = r;
        col = c;
        type = t;
        parent = null;
        g = Integer.MAX_VALUE;
        h = 0;
        f = 0;
    }

    public void setF() {
        f = g + h;
    }

    public void setG(int value) {
        g = value;
    }

    public void setH(int value) {
        h = value;
    }

    public void setParent(Node n) {
        parent = n;
    }

    public int getF() {
        return f;
    }

    public int getG() {
        return g;
    }

    public int getH() {
        return h;
    }

    public Node getParent() {
        return parent;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return row == node.row && col == node.col;
    }

    @Override
    public String toString() {
        return "[" + row + ", " + col + "]";
    }
}