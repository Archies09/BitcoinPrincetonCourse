import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	private double p_graph,p_malicious,p_txDistribution;
	private int numRounds,numberOfFollowees;
    private boolean[] followees;
    private HashSet<Transaction> pendingTransactions;
    private HashSet<Transaction> receivedTransactions;
    private HashSet<Transaction> finalTransactions;
    private HashMap<Integer, Integer> followeeToNumberOfPrevTx = new HashMap<Integer, Integer>();
    private HashMap<Integer, HashSet<Transaction> > followeeToPrevTxList = new HashMap<Integer, HashSet<Transaction> >();
    private HashMap<Integer, Boolean> maliciousOrNot = new HashMap<Integer, Boolean>();
    
	public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph=p_graph;
        this.p_malicious=p_malicious;
        this.p_txDistribution=p_txDistribution;
        this.numRounds=numRounds;
        
    	// IMPLEMENT THIS
    }

    public void setFollowees(boolean[] followees) {
    	this.followees=new boolean[followees.length];
        int countOfFollowees=0;
    	for(int i=0;i<followees.length;i++)
        	{
    			this.followees[i]=followees[i];
    			if(followees[i]==true)
				{
					countOfFollowees++;
					followeeToPrevTxList.put(i,new HashSet<Transaction>());
					followeeToNumberOfPrevTx.put(i,0);
					maliciousOrNot.put(i,false);
				}
        	}
        numberOfFollowees=countOfFollowees;
    	// IMPLEMENT THIS
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions= new HashSet<Transaction>();
    	this.pendingTransactions.addAll(pendingTransactions);
    	this.finalTransactions = new HashSet<Transaction>(); 
    	// IMPLEMENT THIS
    }

    public Set<Transaction> sendToFollowers() {
    	this.finalTransactions.addAll(this.pendingTransactions);
    	HashSet<Transaction> returnTransactions = new HashSet<Transaction>();
    	returnTransactions.addAll(this.finalTransactions);
    	return returnTransactions;
        // IMPLEMENT THIS
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	this.receivedTransactions = new HashSet<Transaction>();
    	HashMap<Integer, Integer> followeeToNumberOfTx = new HashMap<Integer, Integer>();
        HashMap<Integer, HashSet<Transaction> > followeeToTxList = new HashMap<Integer, HashSet<Transaction> >();
        	
    	System.err.println("Malicious List"+maliciousOrNot);

    	for(Candidate c : candidates)
    	{
			receivedTransactions.add(c.tx);
    		if(maliciousOrNot.get(c.sender)==false)
    			{
    				int senderCount;
    				if(followeeToNumberOfTx.get(c.sender)==null)
    					senderCount=0;
    				else senderCount=followeeToNumberOfTx.get(c.sender);
    				senderCount++;
    				followeeToNumberOfTx.put(c.sender,senderCount);
    				if(followeeToTxList.get(c.sender)==null)
    					followeeToTxList.put(c.sender,new HashSet<Transaction>());
    				followeeToTxList.get(c.sender).add(c.tx);
    			}
    	}
    	
    	for(int i=0;i<this.followees.length;i++)
    	{
    		if(this.followees[i]==true)
    		{
    			if(maliciousOrNot.get(i)==false)
    			{
    				if(followeeToNumberOfTx.get(i)==null || followeeToTxList.get(i)==null)
    				{
    					System.err.println("Because of null");
    					maliciousOrNot.put(i, true);
    				}
    				else
    				{
    					System.err.println("FolloweeToTxList:"+followeeToTxList.get(i));
    					System.err.println("FolloweeToPrevTxList:"+followeeToPrevTxList.get(i));
    					System.err.println("FolloweeToNumberTx:"+followeeToNumberOfTx.get(i));
    					System.err.println("FolloweeToNumberPrevTx:"+followeeToNumberOfPrevTx.get(i));
    					System.err.println("Bool value:"+followeeToPrevTxList.get(i).isEmpty());
    					System.err.println("Bool value of contains:"+followeeToTxList.get(i).containsAll(followeeToPrevTxList.get(i)));
    					
    					if(followeeToPrevTxList.get(i).isEmpty()==true)
    					{
	    					followeeToPrevTxList.get(i).addAll(followeeToTxList.get(i));
	    					followeeToNumberOfPrevTx.put(i,followeeToTxList.get(i).size());
    					}
    					
    					else if(followeeToNumberOfPrevTx.get(i)<=followeeToNumberOfTx.get(i) && followeeToTxList.get(i).containsAll(followeeToPrevTxList.get(i))==true)
	    				{
	    					finalTransactions.addAll(followeeToPrevTxList.get(i));
	    					followeeToPrevTxList.get(i).clear();
	    					followeeToPrevTxList.get(i).addAll(followeeToTxList.get(i));
	    					followeeToNumberOfPrevTx.put(i,followeeToTxList.get(i).size());
	    				}
	    				else
	    				{
	    					maliciousOrNot.put(i, true);
	    				}
    				}
    			}
    		}
    	}
    	
    	
    }
}	