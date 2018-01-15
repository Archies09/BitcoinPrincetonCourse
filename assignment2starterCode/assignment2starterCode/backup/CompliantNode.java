import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	private double p_graph,p_malicious,p_txDistribution;
	private int numRounds,numberOfFollowees;
    private boolean[] followees;
    private HashSet<Transaction> pendingTransactions;
    private HashSet<Transaction> receivedTransactions;
    private HashSet<Transaction> finalTransactions;
    private HashMap<Transaction, Integer> txToNumberOfAcceptors = new HashMap<Transaction, Integer>();
    private HashMap<Transaction, HashSet<Integer> > txToAcceptorsList = new HashMap<Transaction, HashSet<Integer> >();
    
    
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
    				countOfFollowees++;
        	}
        numberOfFollowees=countOfFollowees;
    	// IMPLEMENT THIS
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions= new HashSet<Transaction>();
    	this.pendingTransactions.addAll(pendingTransactions);
    	// IMPLEMENT THIS
    }

    public Set<Transaction> sendToFollowers() {
    	this.finalTransactions = new HashSet<Transaction>(); 
    	this.finalTransactions.addAll(this.pendingTransactions);
    	if(this.receivedTransactions!=null)
    	{
    		for(Transaction t : this.receivedTransactions)
    		{
    			if(txToNumberOfAcceptors.get(t)>=(numberOfFollowees/2))
    				this.finalTransactions.add(t);
    		}
    		
    	}
    	return this.finalTransactions;
        // IMPLEMENT THIS
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	this.receivedTransactions = new HashSet<Transaction>();
    	for(Candidate c : candidates)
    	{
    		if(txToAcceptorsList.get(c.tx)==null)
    			{
    				txToAcceptorsList.put(c.tx, new HashSet<Integer>());
    				txToNumberOfAcceptors.put(c.tx,0);
    			}
    		
    		receivedTransactions.add(c.tx);
    		if(txToAcceptorsList.get(c.tx).contains(c.sender)!=true)
    		{
    			txToAcceptorsList.get(c.tx).add(c.sender);
    			int accepto = txToNumberOfAcceptors.get(c.tx);
    			accepto++;
    			txToNumberOfAcceptors.put(c.tx,accepto);
    		}
    	}
    }
}
