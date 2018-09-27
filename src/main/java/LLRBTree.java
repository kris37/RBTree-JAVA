import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @author panqiang37@gmail.com
 * @version 0.1
 * Date: 2018/9/25 上午9:21
 * To change this template use File | Settings | File Templates.
 * Description:
 * <p>
 *     Left-Leaning RBTree
 *      原版参考 《算法》 第四版 https://algs4.cs.princeton.edu/33balanced/RedBlackBST.java.html
 *      中文参考 https://cloud.tencent.com/developer/article/1193097
 *     值得时刻牢记的是：LLRB树的右子树不会比对应的左子树大(if node.left is null ,node.right must be null)
 * <br>
 */
@Deprecated
public class LLRBTree<K extends Comparable , V> {
    public static final boolean RED = true;
    public static final boolean BLACK = false;
    private Node root;

    private class Node{
        private K key;
        private V value;
        private Node left;
        private Node right;
        private int size = 1;
        private boolean color = RED;
        private Node(K key,V value){
            this.key = key;
            this.value = value;
        }
    }

    public void insert(K key,V value){
        if(Objects.isNull(key)) {
            throw new IllegalArgumentException(" insert key is null !");
        }
        root = insert(root,key,value);
        root.color = BLACK;
    }

    public V search(K key){
        if(Objects.isNull(key)){
            throw new IllegalArgumentException(" search key is null");
        }
        Node find = search(root,key);
        return find == null ?null:find.value;
    }
    private Node search(Node node,K key){
        int cmp = 0;
        while (node != null){
            cmp = key.compareTo(node.key);
            if(cmp > 0){
                node = node.right;
            }else if(cmp < 0){
                node = node.left;
            }else {
                return node;
            }
        }
        return null;
    }

    /**
     * 插入的节点是红色
     * @param node
     * @param key
     * @param value
     * @return
     */
    private Node insert(Node node, K key, V value){
        if(Objects.isNull(node)){
            return new Node(key,value);
        }
        int cmp = key.compareTo(node.key);
        if(cmp > 0){
            node.right = insert(node.right,key,value);
        }else if(cmp < 0){
            node.left = insert(node.left,key,value);
        }else {
            node.value = value;
        }
        // fix up LL 就是将红色链接全部转移到左子树，统一进行处理
        // 左黑右红 将 RL => LL 这里比 fixUpAferDelete 多了一个判断左黑的部分，是减少左边是红色的时候无厘头的旋转
        if(!isRed(node.left) && isRed(node.right)){
            node = leftRotate(node);
        }
        //
        if(isRed(node.left) && isRed(node.left.left)){
            node = rightRotate(node);
        }
        if(isRed(node.left) && isRed(node.right)){
            node.left.color = BLACK;
            node.right.color = BLACK;
            node.color = !node.color;
        }
        node.size = reComputeSize(node);
        return node;
    }

    public void delete(K key){
        if(search(key) == null ){
            return ;
        }
        // 如果 root 左右都是 黑则先将 root 涂红
        if(isRed(root.left) && !isRed(root.right)){
            root.color = RED;
        }
        root = delete(root,key);
        if(null != root) {
            root.color = BLACK;
        }
    }

    /**
     *
     * 删除同样是找替换节点，然后删除替换节点。替换节点 只有一个RED节点或者没有子节点
     *  1。删除的当前节点不能是2-node（就是黑色节点，且无子节点）
        2。如果有必要可以变换成4-node
        3。从底部删除节点
        4。向上的fix过程中，消除4-node
     * @param node
     * @param key
     * @return
     */
    private Node delete(Node node,K key){if (key.compareTo(node.key) < 0)  {
        if (!isRed(node.left) && !isRed(node.left.left))
            node = moveRed2Left(node);
        node.left = delete(node.left, key);
    }
    else {
        if (isRed(node.left))
            node = rightRotate(node);
        if (key.compareTo(node.key) == 0 && (node.right == null))
            return null;
        if (!isRed(node.right) && !isRed(node.right.left))
            node = moveRed2Right(node);
        if (key.compareTo(node.key) == 0) {
            Node x = findMin(node.right);
            node.key = x.key;
            node.value = x.value;
            // h.val = get(h.right, min(h.right).key);
            // h.key = min(h.right).key;
            node.right = deleteMin(node.right);
        }
        else node.right = delete(node.right, key);
    }
        return fixUpAfterDelete(node);
    }

    /**
     *
     * 删除节点后修复函数
     * 依然要遵守 LL规则
     * @param node
     * @return
     */
    private Node fixUpAfterDelete(Node node) {
        if (isRed(node.right)) {
            node = leftRotate(node);
        }
        if (isRed(node.left) && isRed(node.left.left)) {
            node = rightRotate(node);
        }
        if (isRed(node.left) && isRed(node.right)){
            flipColor(node);
        }
        return node;
    }

    private Node findMin(Node node){
        while (node != null){
            node = node.left;
        }
        return node;
    }

    /**
     * 删除node 子树中最小节点，那么被删除的节点一定不存在左节点
     * 自上而下的调整树的颜色
     * @param node
     * @return
     */
    private Node deleteMin(Node node){

        if(node.left == null){
            return node.right;
        }
        if(!isRed(node.left) && !isRed(node.left.left)){
            node = moveRed2Left(node);
        }
        node.left = deleteMin(node.left);
        return fixUpAfterDelete(node);

    }

    /**
     * 1. 将左倾 => 右倾 (LL => RL)
     * 2.       x
     *         / \
     *        y   z
     *       /\   /\
     *      ∂  ß ƒ µ
     *    (z and f is BLACK)
     * @param node
     * @return
     */
    private Node deleteMax(Node node){

        if(isRed(node.left)){
            node = rightRotate(node);
        }
        if(node.right == null){
            return null;
        }
        /** 实质上 经过 1。的 LL => RL 此时 node.left must be BLACK
         *  如果右节点不是 2-3-4 树的 2节点（即 node.right 至少有一个红色节点，又因为LL树的红色节点肯定是在左边）
         *  则从父节点或者兄弟节点借 一个节点
         */

        if(!isRed(node.right) && !isRed(node.right.left)){
            node = moveRed2Right(node);
        }
        node.right = deleteMax(node.right);
        return fixUpAfterDelete(node);
    }

    /**
     *  1.将红色下推
     *  2.如果 node.left.left is RED 则将其转移到右子树（相当于2-3-4 树node.right 从左兄弟节点借一个节点）并恢复父节点及子节点颜色
     *   如果不是 则从父节点 借一个节点 形成 3-4 节点
     * @return
     */
    private Node moveRed2Right(Node node){
        flipColor(node);
        if(isRed(node.left.left)){
            node = rightRotate(node);
            flipColor(node);
        }
        return node;
    }

    /**
     * 逻辑 同上
     * 只不过这次是从 右兄弟节点借（LL）
     * @param node
     * @return
     */
    private Node moveRed2Left(Node node){
        flipColor(node);

        if(isRed(node.right.left)){
            node.right = rightRotate(node.right);
            node = leftRotate(node);
            flipColor(node);
        }
        return node;
    }
    private void flipColor(Node node) {
        node.color = !node.color;
        node.left.color = !node.left.color;
        node.right.color = !node.right.color;
    }


    /**
     *      x         y
     *     / \       / \
     *    ∂  y   => x   ç
     *      / \    ／\
     *     ß   ç   ∂ ß
     *
     * x,y 互换颜色并且调整 size
     * @param x
     * @return
     */
    private Node leftRotate(Node x){
        Node y = x.right;
        boolean color =  y.color;
        x.right = y.left;
        y.left = x;
        y.color = x.color;
        x.color  = color;
        x.size = reComputeSize(x);
        y.size = reComputeSize(y);
        return y;
    }

    private Node rightRotate(Node x){
        Node y = x.left;
        boolean color =  y.color;
        x.left = y.right;
        y.right = x;
        y.color = x.color;
        x.color  = color;
        x.size = reComputeSize(x);
        y.size = reComputeSize(y);
        return y;
    }


    public int size(){
        return size(root);
    }
    private int size(Node node){
        return node == null ? 0 : node.size;
    }
    private int reComputeSize(Node node){
        return node == null? 0 : size(node.left) + size(node.right) + 1 ;
    }

    private boolean isRed(Node node){
        return node == null ? false : node.color;
    }


    public static void main(String [] args){
        ArrayList<Integer> integers = Lists.newArrayList( 10,11,12,13,14,15,16,17,18,19,20,1, 2, 3, 4, 5, 6, 7, 8, 9);
        LLRBTree<Integer, Integer> integerIntegerRedBlackTree = new LLRBTree<>();
        integers.forEach(each -> integerIntegerRedBlackTree.insert(each,each));

        ArrayList<Integer> deleteList = Lists.newArrayList( 2,5,3, 4, 5, 13,6, 7, 8, 9, 10,16,20,17,18,19,11,12,13,1,14);
        deleteList.forEach(each -> integerIntegerRedBlackTree.delete(each));

    }
}
