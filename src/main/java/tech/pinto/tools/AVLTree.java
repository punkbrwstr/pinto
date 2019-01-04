package tech.pinto.tools;


class Node { 
	double key;
	int count = 1;
    int height = 1; 
    int childCount = 0;
    Node left, right; 
  
    Node(double d) { 
        key = d; 
    } 
} 
  
public class AVLTree { 
    Node root; 
    int size = 0;
  
    
    public void insert(double... keys) {
        for (double key : keys) {
            root = insert(root, key);
        }
    }
    
    public void delete(double... keys) {
        for (double key : keys) {
            root = deleteNode(root, key);
        	size--;
        }
    }
    
    public void clear() {
    	root = null;
    	size = 0;
    }
    
    public boolean isEmpty() {
    	return root == null;
    }

    /**
     * Computes the median of this tree. Makes at most two calls to logarithmic 
     * time methods.
     * 
     * @return the median of this tree.
     */
    public double getMedian() {
        if (size == 0) {
            throw new IllegalStateException(
                    "Asking for median from an empty tree.");
        }

        if (size % 2 == 0) {
            double b = getNode(root, size / 2 - 1).key;
            double a = getNode(root, size / 2).key;
            return 0.5 * (a + b);
        } else {
            return getNode(root, size / 2).key;
        }
    }
    
    public double getMin() {
    	return minValueNode(root).key;
    }
    
    public double getMax() {
    	return maxValueNode(root).key;
    }
    
    private Node getNode(Node root, int index) {
        Node current = root;

        for (;;) {
            int leftSubtreeSize = getLeftSubtreeSize(current);

            if (index >= leftSubtreeSize && index <= leftSubtreeSize + current.count - 1) {
                return current;
            }

            if (index > leftSubtreeSize) {
                //index -= (leftSubtreeSize + 1);
                index -= (leftSubtreeSize + current.count);
                current = current.right;
            } else {
                current = current.left;
            }
        }
    }
    
    private int getLeftSubtreeSize(Node node) {
        int tmp = node.childCount;

        if (node.right != null) {
            tmp -= (node.right.childCount + node.right.count);
        }

        return tmp;
    }
    
    // A utility function to get height of the tree 
    private int height(Node N) { 
        if (N == null) {
             return 0; 
        }
        return N.height; 
    } 
  
    private void fixHeightAndChildCount(Node p) {
        int hl = height(p.left);
        int hr = height(p.right);
        p.height = (hl > hr ? hl : hr) + 1;
        p.childCount = 0;
        if (p.left != null) {
            p.childCount = p.left.childCount + p.left.count;
        }
        if (p.right != null) {
            p.childCount += p.right.childCount + p.right.count;
        }
    }
  
    // A utility function to right rotate subtree rooted with y 
    // See the diagram given above. 
    private Node rightRotate(Node y) { 
        Node x = y.left; 
        Node T2 = x.right; 
  
        // Perform rotation 
        x.right = y; 
        y.left = T2; 
  
        // Update heights 
        fixHeightAndChildCount(y);
        fixHeightAndChildCount(x);
  
        // Return new root 
        return x; 
    } 
  
    // A utility function to left rotate subtree rooted with x 
    // See the diagram given above. 
    private Node leftRotate(Node x) { 
        Node y = x.right; 
        Node T2 = y.left; 
  
        // Perform rotation 
        y.left = x; 
        x.right = T2; 
  
        //  Update heights 
        fixHeightAndChildCount(x);
        fixHeightAndChildCount(y);
  
        // Return new root 
        return y; 
    } 
  
    // Get Balance factor of node N 
    private int getBalance(Node N) { 
        if (N == null) 
            return 0; 
        return height(N.left) - height(N.right); 
    } 
  
    private Node insert(Node node, double key) { 
        /* 1.  Perform the normal BST rotation */
        if (node == null) { 
        	++size;
            return (new Node(key)); 
        }
  
        if (key < node.key) 
            node.left = insert(node.left, key); 
        else if (key > node.key) 
            node.right = insert(node.right, key); 
        else {
        	size++;
        	node.count++;
            return node; 
        }
  
        /* 2. Update height of this ancestor node */
        fixHeightAndChildCount(node);
  
        /* 3. Get the balance factor of this ancestor 
           node to check whether this node became 
           Wunbalanced */
        int balance = getBalance(node); 
  
        // If this node becomes unbalanced, then 
        // there are 4 cases Left Left Case 
        if (balance > 1 && key < node.left.key) 
            return rightRotate(node); 
  
        // Right Right Case 
        if (balance < -1 && key > node.right.key) 
            return leftRotate(node); 
  
        // Left Right Case 
        if (balance > 1 && key > node.left.key) 
        { 
            node.left = leftRotate(node.left); 
            return rightRotate(node); 
        } 
  
        // Right Left Case 
        if (balance < -1 && key < node.right.key) 
        { 
            node.right = rightRotate(node.right); 
            return leftRotate(node); 
        } 
  
        /* return the (unchanged) node pointer */
        return node; 
    } 
  
    /* Given a non-empty binary search tree, return the 
       node with minimum key value found in that tree. 
       Note that the entire tree does not need to be 
       searched. */
    private Node minValueNode(Node node) { 
        Node current = node; 
  
        /* loop down to find the leftmost leaf */
        while (current.left != null) 
           current = current.left; 
  
        return current; 
    } 

    private Node maxValueNode(Node node) { 
        Node current = node; 
  
        /* loop down to find the leftmost leaf */
        while (current.right != null) 
           current = current.right; 
  
        return current; 
    } 
  
    private Node deleteNode(Node root, double key) { 
        // STEP 1: PERFORM STANDARD BST DELETE 
        if (root == null) { 
            return root; 
        }
  
        // If the key to be deleted is smaller than 
        // the root's key, then it lies in left subtree 
        if (key < root.key) 
            root.left = deleteNode(root.left, key); 
  
        // If the key to be deleted is greater than the 
        // root's key, then it lies in right subtree 
        else if (key > root.key) 
            root.right = deleteNode(root.right, key); 
  
        // if key is same as root's key, then this is the node 
        // to be deleted 
        else { 
        	if(root.count > 1) {
        		root.count--;
        		return root;
        	}
            // node with only one child or no child 
            if ((root.left == null) || (root.right == null)) 
            { 
                Node temp = null; 
                if (temp == root.left) 
                    temp = root.right; 
                else
                    temp = root.left; 
  
                // No child case 
                if (temp == null) 
                { 
                    temp = root; 
                    root = null; 
                } 
                else   // One child case 
                    root = temp; // Copy the contents of 
                                 // the non-empty child 
            } 
            else
            { 
  
                // node with two children: Get the inorder 
                // successor (smallest in the right subtree) 
                Node temp = minValueNode(root.right); 
  
                // Copy the inorder successor's data to this node 
                root.key = temp.key; 
  
                // Delete the inorder successor 
                root.right = deleteNode(root.right, temp.key); 
            } 
        } 
  
        // If the tree had only one node then return 
        if (root == null) 
            return root; 
  
        // STEP 2: UPDATE HEIGHT OF THE CURRENT NODE 
        fixHeightAndChildCount(root);
  
        // STEP 3: GET THE BALANCE FACTOR OF THIS NODE (to check whether 
        //  this node became unbalanced) 
        int balance = getBalance(root); 
  
        // If this node becomes unbalanced, then there are 4 cases 
        // Left Left Case 
        if (balance > 1 && getBalance(root.left) >= 0) 
            return rightRotate(root); 
  
        // Left Right Case 
        if (balance > 1 && getBalance(root.left) < 0) 
        { 
            root.left = leftRotate(root.left); 
            return rightRotate(root); 
        } 
  
        // Right Right Case 
        if (balance < -1 && getBalance(root.right) <= 0) 
            return leftRotate(root); 
  
        // Right Left Case 
        if (balance < -1 && getBalance(root.right) > 0) 
        { 
            root.right = rightRotate(root.right); 
            return leftRotate(root); 
        } 
  
        return root; 
    } 
  
    // A utility function to print preorder traversal of 
    // the tree. The function also prints height of every 
    // node 
    void preOrder(Node node) 
    { 
        if (node != null) 
        { 
            System.out.print(node.key + (node.count > 1 ? "(" + node.count + ")" : "") + " "); 
            preOrder(node.left); 
            preOrder(node.right); 
        } 
    } 
  
    public static void main(String[] args) 
    { 
        AVLTree tree = new AVLTree(); 
  
        /* Constructing tree given in the above figure */
        tree.root = tree.insert(tree.root, 9); 
        tree.root = tree.insert(tree.root, 5); 
        tree.root = tree.insert(tree.root, 6); 
        tree.root = tree.insert(tree.root, 10); 
        tree.root = tree.insert(tree.root, 0); 
        tree.root = tree.insert(tree.root, 6); 
        tree.root = tree.insert(tree.root, 11); 
        tree.root = tree.insert(tree.root, -1); 
        tree.root = tree.insert(tree.root, 1); 
        tree.root = tree.insert(tree.root, 2); 
  
        /* The constructed AVL Tree would be 
           9 
          /  \ 
         1    10 
        /  \    \ 
        0    5    11 
        /    /  \ 
        -1   2    6 
         */
        System.out.println("Preorder traversal of "+ 
                            "constructed tree is : "); 
        tree.preOrder(tree.root); 
        System.out.println("Median of "+ 
                            "constructed tree is : " + tree.getMedian()); 
  
        tree.delete(6); 
  
        /* The AVL Tree after deletion of 10 
           1 
          /  \ 
         0    9 
        /     / \ 
        -1    5   11 
        /  \ 
        2    6 
         */
        System.out.println(""); 
        System.out.println("Preorder traversal after "+ 
                           "deletion of 10 :"); 
        tree.preOrder(tree.root); 
        System.out.println("Median of "+ 
                            "constructed tree is : " + tree.getMedian()); 
    } 
} 