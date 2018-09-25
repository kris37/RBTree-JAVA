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
 * <br>
 */
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
     *
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
        // 左黑右红 将 RL => LL
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

    }

    /**
     *
     * @param node
     * @param key
     * @return
     */
    private Node delete(Node node,K key){
        //todo delete logstic
        return null;
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
    }
}
