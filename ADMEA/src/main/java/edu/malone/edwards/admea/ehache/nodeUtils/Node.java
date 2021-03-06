/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.malone.edwards.admea.ehache.nodeUtils;

import static edu.malone.edwards.admea.ehache.ASystem.qLearningQueue;
import edu.malone.edwards.admea.ehache.qlearning.QLearning;
import gnu.trove.map.hash.THashMap;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.math3.distribution.BinomialDistribution;

/**
 * Each Node has: <br>
 * <ul>
 *      <li>the elements to operate a binomial distribution</li>
 *      <li>a list of all of its children Nodes</li>
 *      <li>a score given to it by the process used for Q learning</li>
 *      <li>a policy given to it from Q learning of the scores to every other Node</li>
 *      <li>a unique state given to it by the process</li>
 *      <li>a unique String Id</li>
 * </ul>
 * @author Cory Edwards
 * 
 */
public class Node implements Serializable {
    
    /**
     * probability for binomial distribution.
     */
    private double p = 1.0;
    
    /**
     * n or number of trials in binomial distribution.
     */
    private int n = 0;
    
    /**
     * k or number of successes in binomial distribution.
     */
    private int k = 0;
    
    /**
     * The Node's Id that can be referenced from other Nodes.
     */
    private final String id;
    
    /**
     * All children Nodes or paths to take.
     */
    public Set<String> children = new HashSet();
    
    /**
     * Every score for every other Node given this is the current Node obtained after Q learning.
     * If a Node is not a child of this Node then its score will be 0.
     * THashMap needs much less memory than normal HashMap, which is why it is used.
     */
    private final THashMap<String, Double> policy;
    
    /**
     * The score that Q learning will use to build a policy.
     */
    private  int score;
    
    /**
     * The state that this Node is a reference for.
     */
    private final State state;
    
    /**
     * Boolean to tell if a Node needs to be initialized or not.
     */
    private boolean isNew;
    
    public  String[] parents = new String[0];
    
    /**
     * Create a new Node for a new state.
     * @param givenState The state that does not have a node yet.
     * @param givenId The unique id for this node.
     */
    public Node(State givenState, String givenId)
    {
        policy = new THashMap();
        state = givenState;
        id = givenId;
        isNew = true;
    }
    
    public synchronized void addParent(Node parent)
    {
        String[] temp = new String[parents.length + 1];
        System.arraycopy(parents, 0, temp, 0, parents.length);
        temp[temp.length - 1] = parent.id;
        parents = temp;
    }
    
    /**
     * After Q learning has made all of the policies, it will give this Node
     * its policy.
     * @param newPolicy The given policy from Q learning.
     */
    public void givePolicy(THashMap<String, Double> newPolicy)
    {
        policy.clear();
        policy.putAll(newPolicy);
        //Needs to either be put back, or updated

    }
    
    /**
     * Every score for every other Node given this is the current Node obtained after Q learning.
     * If a Node is not a child of this Node then its score will be 0.
     * THashMap needs much less memory than normal HashMap, which is why it is used.
     * @return Get the policy for this Node.
     */
    public THashMap<String, Double> getPolicy()
    {
        return policy;
    }
    
    protected int getN()
    {
        return n;
    }
    
    protected int getK()
    {
        return k;
    }
    
    /**
     * @return The winning score obtained from Q learning.
     */
    public double getMaxQScore()
    {
        double max = -Double.MAX_VALUE;
        for(Double QScore : policy.values())
        {
            if(QScore > max)
                max = QScore;
        }
        
        if(max == -Double.MAX_VALUE)
            return (double) score;
        else
            return max;
    }
    
    /**
     * @return The winning child with the highest score from Q learning.
     */
    public String getMaxQChild()
    {
        double max = Double.MIN_VALUE;
        String winningChildId = id;
        for(String childId : policy.keySet())
        {
            if(policy.get(childId) > max)
            {
                max = policy.get(childId);
                winningChildId = childId;
                
            }
        }
        
        if(winningChildId == null ? id == null : winningChildId.equals(id))
        {
            if(children.isEmpty())
                return id;
            else
                return children.toArray()[0].toString();
        }
        else
            return winningChildId;
    }
    
    /**
     * 
     * @return The Node's unique state.
     */
    public State getState()
    {
        return state;
    }
    
    /**
     * To determine if this Node needs to be initialized or not.
     * @return True if and only if the Node is new.
     */
    public boolean isNew()
    {
        boolean temp = isNew;
        isNew = (temp == true ? !isNew : isNew);
        return temp;
    }
    
    /**
     * Add 1 to n in the Node's binomial distribution.
     */
    public void addOccurrence()
    {
        n = n + 1 > Integer.MAX_VALUE ? Integer.MAX_VALUE : n + 1;
    }
    
    /**
     * Add 1 to k in the Node's binomial distribution.
     */
    public void addSuccess()
    {
        k = k + 1 > Integer.MAX_VALUE ? Integer.MAX_VALUE : k + 1;
    }
    
    /**
     * Recalculate the probability that will be used in the Node's 
     * binomial distribution: (k / n)
     */
    public void recalcProb()
    {
        p = (float) k / n;
        //Needs to be put back, or updated fired.
    }
    
    /**
     * The programmer can use this to, rather than automatically calculate
     * the probability, they can give the Node its probability.
     * @param newP The Node's probability.
     */
    public void giveProb(double newP)
    {
        p = newP;
        //Needs to be put back, or updated fired.//Needs to be put back, or updated fired.
    }
    
    /**
     * 
     * @return The Node's score that was calculated when it was initialized.
     */
    public int getScore()
    {
        return score;
    }
    
    /**
     * When the Node's is initialized, it will have to give a score to finish.
     * When the score is given, it cannot be changed later. Calling this method
     * more than once will not do anything.
     * @param givenScore The Node's calculated score.
     */
    public void setScore(int givenScore)
    {
        score = givenScore;
    }
    
    /**
     * Calculate the binomial probability of this node.
     * @return The current probability from a binomial distribution.
     */
    public double calculateProbability()
    {
        if(n != 1)
            return new BinomialDistribution(n, p).cumulativeProbability(k);
        else
            return 1.0;
    }
    
    /**
     * @return The Node's unique Id.
     */
    public String getNodeId()
    {
        return id;
    }

    public void update()
    {
        qLearningQueue.submit(new QLearning(this));
//        System.out.println("Updated " + id);
    }
}
