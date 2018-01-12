import java.util.*;

public class TxHandler {

    UTXOPool publicLedger,doubleSpendCheck;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
   	publicLedger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
	double sumOfInputs=0;
	doubleSpendCheck = new UTXOPool();
	for(int i=0;i<tx.numInputs();i++)
	{
		if(publicLedger.contains(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex)))
		{
		
			if(doubleSpendCheck.contains(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex))==false)
			{
				doubleSpendCheck.addUTXO(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex),publicLedger.getTxOutput(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex)));
				if(Crypto.verifySignature(publicLedger.getTxOutput(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex)).address,tx.getRawDataToSign(i),tx.getInput(i).signature))
				{
			
					sumOfInputs+=(publicLedger.getTxOutput(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex))).value;
				
				}			
				else return false;
			}
			else return false;
		}
		else return false;
	}
	
	double sumOfOutputs=0;
	for(int i=0;i<tx.numOutputs();i++)
	{
		if(tx.getOutput(i).value<0)
			return false;
		else	sumOfOutputs+=tx.getOutput(i).value;
	}

	if(sumOfInputs<sumOfOutputs)
		return false;
        return true;
    }

    HashMap<Transaction,HashSet<Transaction> > dependencyGraph = new HashMap<Transaction,HashSet<Transaction> >();
    HashSet<Transaction> possibleTxsSet = new HashSet<Transaction>();
    List<Transaction> topoOrder = new ArrayList<Transaction>();
    
    void topoOrdering(Transaction t, HashMap<Transaction,Boolean > visitedMap, Stack<Transaction> stackOfTx)
    {
    	visitedMap.put(t,true);
    	Transaction r;
    	
    	Iterator<Transaction> txIterator = dependencyGraph.get(t).iterator();
    	while(txIterator.hasNext())
    	{
    		r=txIterator.next();
    		if(visitedMap.get(r)==null)
    			topoOrdering(r, visitedMap, stackOfTx);
    	}
    	
    	stackOfTx.push(t);
    }
    
    void findTopoSort()
    {
    	Stack<Transaction> stackOfTx = new Stack<Transaction>();
    	HashMap<Transaction,Boolean> visitedMap = new HashMap<Transaction,Boolean>();
    	for(Transaction t : possibleTxsSet)
    	{
    		if(visitedMap.get(t)==null)
    			topoOrdering(t,visitedMap,stackOfTx);
    	}
    	
    	while(!stackOfTx.empty())
    	{
    		topoOrder.add(stackOfTx.pop());
    	}
    }
    
    
    
    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
	    
    possibleTxsSet.addAll(Arrays.asList(possibleTxs));	
    for(Transaction t : possibleTxs)
    {
    	dependencyGraph.put(t, new HashSet<Transaction>());
    	
    	for(int i=0;i<t.numOutputs();i++)
    	{
    		UTXO testUTXO = new UTXO(t.getHash(),i);
	    	
    		Transaction qDepend=null;
    		
    		int testFlag=0;
    		
    		for(Transaction q : possibleTxs)
    	    {
    			for(int j=0;j<q.numInputs();j++)
    	    	{
    				UTXO qUTXO = new UTXO(q.getInput(j).prevTxHash,q.getInput(j).outputIndex);
    				if(qUTXO.equals(testUTXO))
    				{
    					testFlag=1;
    					qDepend=q;
    					dependencyGraph.get(t).add(qDepend);
    				}
    			}
    	    }
    		
    	}
    	
    }
    
    System.err.println("Dependency Graph is:");
    for(Transaction t : possibleTxs)
    {
    	System.err.println(t+" : "+dependencyGraph.get(t));
    }
    
	
    findTopoSort();
    
    System.err.println(topoOrder);
    List<Transaction> maximalSetOfValidTransaction = new ArrayList<>();
    
	for(Transaction t : topoOrder)
	{
		if(isValidTx(t))
		{
			maximalSetOfValidTransaction.add(t);
	

			for(int i=0;i<t.numInputs();i++)
	        	{
					publicLedger.removeUTXO(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
	        	}
		
			for(int i=0;i<t.numOutputs();i++)
	        	{
	               	publicLedger.addUTXO(new UTXO(t.getHash(),i),t.getOutput(i));
	        	}	
			
		}

	}	
	Transaction [] maximalSetOfValidTransactionArray = new Transaction[maximalSetOfValidTransaction.size()]; 
	return maximalSetOfValidTransaction.toArray(maximalSetOfValidTransactionArray);
        // IMPLEMENT THIS
    }

}
