import java.util.*;

public class MaxFeeTxHandler {

    UTXOPool publicLedger,doubleSpendCheck;
    
    public MaxFeeTxHandler(UTXOPool utxoPool) {
    publicLedger = new UTXOPool(utxoPool);
    }

    public boolean isValidTx(Transaction tx) {
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
    
    
    public boolean isValidWithPool(Transaction tx,UTXOPool currentPool) {
    	double sumOfInputs=0;
    	doubleSpendCheck = new UTXOPool();
    	for(int i=0;i<tx.numInputs();i++)
    	{
    		if(currentPool.contains(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex)))
    		{
    		
    			if(doubleSpendCheck.contains(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex))==false)
    			{
    				doubleSpendCheck.addUTXO(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex),publicLedger.getTxOutput(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex)));
    				if(Crypto.verifySignature(currentPool.getTxOutput(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex)).address,tx.getRawDataToSign(i),tx.getInput(i).signature))
    				{
    			
    					sumOfInputs+=(currentPool.getTxOutput(new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex))).value;
    				
    				}			
    				else {System.err.println("Wrong crypto"+tx);return false;}
    			}
    			else {System.err.println("Double Spending"+tx);return false;}
    		}
    		else {System.err.println("Not in currentpool"+tx);return false;}
    	}
    	
    	double sumOfOutputs=0;
    	for(int i=0;i<tx.numOutputs();i++)
    	{
    		if(tx.getOutput(i).value<0)
    			{System.err.println("Op value is low for"+tx);return false;}
    		else	sumOfOutputs+=tx.getOutput(i).value;
    	}

    	if(sumOfInputs<sumOfOutputs)
    		{System.err.println("Op value is high for"+tx);return false;}
            return true;
        }
    
    
    
    
    
    
    
    
    public double profitCalculator(Transaction tx,UTXOPool copyOfPublicLedger)
    {
    	double sumInputs = 0;
        double sumOutputs = 0;
        for (Transaction.Input in : tx.getInputs())
        {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output txOutput = copyOfPublicLedger.getTxOutput(utxo);
            sumInputs += txOutput.value;
        }
        for (Transaction.Output out : tx.getOutputs()) 
        {
            sumOutputs += out.value;
        }
        return sumInputs - sumOutputs;
    }
    
    
    public class ReturnValues{
    	public double returnFee;
    	public ArrayList<Transaction> returnTransactionList;
    	
    	public ReturnValues(double returnFee, ArrayList<Transaction> returnTransactionList)
    	{
    		
    		this.returnFee=returnFee;
    		this.returnTransactionList=returnTransactionList;
    	}
    	
    	public ArrayList<Transaction> getList()
    	{
    		return returnTransactionList;
    	}
    	
    	public double getFee()
    	{
    		return returnFee;
    	}
    	
    }

    HashMap<Transaction,HashSet<Transaction> > dependencyGraph = new HashMap<Transaction,HashSet<Transaction> >();
    HashMap<Transaction,HashSet<Transaction> > dependencyList = new HashMap<Transaction,HashSet<Transaction> >();
    HashMap<Transaction,HashSet<Transaction> > dependencyMapList = new HashMap<Transaction,HashSet<Transaction> >();
    HashMap<Transaction,Boolean> dependencyMap = new HashMap<Transaction,Boolean>();
    
    HashSet<Transaction> possibleTxsSet = new HashSet<Transaction>();
    List<Transaction> topoOrder = new ArrayList<Transaction>();
    ArrayList<HashSet<Transaction> > arrayOfSubSets = new ArrayList<HashSet<Transaction> >(); 
    int globalcounter=0, firstLoop=0;
    
    void topoOrdering(Transaction t, HashMap<Transaction,Boolean > visitedMap, Stack<Transaction> stackOfTx)
    {
    	visitedMap.put(t,true);
    	Transaction r;
    	
    	Iterator<Transaction> txIterator = dependencyGraph.get(t).iterator();
    	while(txIterator.hasNext())
    	{
    		r=txIterator.next();
    		if(visitedMap.get(r)==null && arrayOfSubSets.get(globalcounter).contains(r)==true)
    			topoOrdering(r, visitedMap, stackOfTx);
    	}
    	
    	stackOfTx.push(t);
    }
    
    void findTopoSort()
    {
    	Stack<Transaction> stackOfTx = new Stack<Transaction>();
    	HashMap<Transaction,Boolean> visitedMap = new HashMap<Transaction,Boolean>();
    	for(Transaction t : arrayOfSubSets.get(globalcounter))
    	{
    		if(visitedMap.get(t)==null)
    			topoOrdering(t,visitedMap,stackOfTx);
    	}
    	
    	while(!stackOfTx.empty())
    	{
    		topoOrder.add(stackOfTx.pop());
    	}
    }
    
    
    HashSet<Transaction> listOfConnectedTx = new HashSet<Transaction>();
    void dfsOrdering(Transaction t, HashMap<Transaction,Boolean > visitedMap)
    {
    	visitedMap.put(t, true);
    	Transaction r;
    	
    	Iterator<Transaction> txIterator = dependencyGraph.get(t).iterator();
    	while(txIterator.hasNext())
    	{
    		r=txIterator.next();
    		if(visitedMap.get(r)==null)
			{
    			listOfConnectedTx.add(r);
    			dfsOrdering(r, visitedMap);
			}
    	}
    	
    }
    
    void dfs()
    {
    	for(Transaction t : possibleTxsSet)
    	{
    		HashMap<Transaction,Boolean> visitedMap = new HashMap<Transaction,Boolean>();
    		dfsOrdering(t,visitedMap);
    		HashSet<Transaction> txmap = new HashSet<Transaction>();
    		txmap.addAll(listOfConnectedTx);
    		dependencyList.put(t,txmap);
    		listOfConnectedTx.clear();
    	}
    	
    }
    
    
    public UTXOPool createCopyOfUtxoPool(UTXOPool originalUtxoPool)
    {
    	UTXOPool returnPool = new UTXOPool();
    	HashSet<UTXO> copyUtxoList = new HashSet<UTXO>();
    	copyUtxoList.addAll(originalUtxoPool.getAllUTXO());
    	for(UTXO u : copyUtxoList)
    	{
    		returnPool.addUTXO(u,originalUtxoPool.getTxOutput(u));
    	}
    	
    	return returnPool;
    }
    
    HashMap<UTXO,HashSet<Transaction> > utxoToListOfTransactions = new HashMap<UTXO,HashSet<Transaction> >();
    HashMap<Transaction, HashSet<Transaction> > conflictingTx = new HashMap<Transaction, HashSet<Transaction> >();
 
    void subsetConstructor(UTXOPool presentPool,HashSet<Transaction> solutionSubset, HashSet<Transaction> hpossibleTx,HashSet<Transaction> oldhpossibleTx)
    {
    	
    	int subFlag=0;
        
    	
    	Iterator<Transaction> titer = hpossibleTx.iterator();
    	while(titer.hasNext())
    	{
    		Transaction t = titer.next();
    		if(conflictingTx.get(t).isEmpty()==true && dependencyMap.get(t)==true)
    		{
    			if(solutionSubset.containsAll(dependencyMapList.get(t))==true)
    			{
    				titer.remove();
    				solutionSubset.add(t);
    			}
    		}
    		else if(conflictingTx.get(t).isEmpty()==false && dependencyMap.get(t)==true)
    		{
    			if(solutionSubset.containsAll(dependencyMapList.get(t))==true)
    			{
    				titer.remove();
    				UTXOPool newPresentPool;
        			newPresentPool=createCopyOfUtxoPool(presentPool);
        			HashSet<Transaction> newHPossibleTx = new HashSet<Transaction>();
    				HashSet<Transaction> newSolutionSet = new HashSet<Transaction>();
    				newSolutionSet.addAll(solutionSubset);
        			newHPossibleTx.addAll(hpossibleTx);
        			newSolutionSet.add(t);
        			newHPossibleTx.removeAll(conflictingTx.get(t));
        			newHPossibleTx.remove(t);
        			for(int i=0;i<t.numInputs();i++)
    	        	{
    					newPresentPool.removeUTXO(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
    	        	}
    		
    				for(int i=0;i<t.numOutputs();i++)
    	        	{
    	               	newPresentPool.addUTXO(new UTXO(t.getHash(),i),t.getOutput(i));
    	        	}
    				subsetConstructor(newPresentPool, newSolutionSet,newHPossibleTx,hpossibleTx);
        			
    			}
    		}
    		else if(conflictingTx.get(t).isEmpty()==false && dependencyMap.get(t)==false)
    		{
				titer.remove();
    			UTXOPool newPresentPool;
    			newPresentPool=createCopyOfUtxoPool(presentPool);
    			HashSet<Transaction> newHPossibleTx = new HashSet<Transaction>();
				HashSet<Transaction> newSolutionSet = new HashSet<Transaction>();
				newSolutionSet.addAll(solutionSubset);
    			newHPossibleTx.addAll(hpossibleTx);
    			newSolutionSet.add(t);
    			newHPossibleTx.removeAll(conflictingTx.get(t));
    			newHPossibleTx.remove(t);
    			for(int i=0;i<t.numInputs();i++)
	        	{
					newPresentPool.removeUTXO(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
	        	}
		
				for(int i=0;i<t.numOutputs();i++)
	        	{
	               	newPresentPool.addUTXO(new UTXO(t.getHash(),i),t.getOutput(i));
	        	}
				subsetConstructor(newPresentPool, newSolutionSet,newHPossibleTx,hpossibleTx);
    			
    		}
    		
    		
    	}
    	
    		if(!arrayOfSubSets.contains(solutionSubset))
    		{
    			arrayOfSubSets.add(solutionSubset);
    			globalcounter++;
    		}
    		return;
    	
    	
    	
    }
    
    
    
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
    
    
    for(Transaction g : possibleTxs)
    {
    	System.err.println("Dependency Graph of "+g+" is "+dependencyGraph.get(g));
    }
    dfs();
    for(Transaction g : possibleTxs)
    {
    	System.err.println("Dependency List of "+g+" is "+dependencyList.get(g));
    }
    
    HashSet<Transaction> emptySolutionSubSet = new HashSet<Transaction>();
    HashSet<Transaction> oldemptySolutionSubSet = new HashSet<Transaction>();

    
    for(Transaction t : possibleTxs)
    {
    	for(int i=0;i<t.numInputs();i++)
		{
			UTXO checkUtxo = new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex);
			if(checkUtxo!=null)
			{
					if(utxoToListOfTransactions.get(checkUtxo)==null)
					{	
						utxoToListOfTransactions.put(checkUtxo, new HashSet<Transaction>());
					}
					utxoToListOfTransactions.get(checkUtxo).add(t);
			}
		}
    }
    
    
    for(Transaction t : possibleTxs)
    {

    	Set<UTXO> utxoKeySet = utxoToListOfTransactions.keySet();
    	
    	for(UTXO u : utxoKeySet)
    	{
    		if(utxoToListOfTransactions.get(u).contains(t))
    		{
    			if(conflictingTx.get(t)==null)
    				conflictingTx.put(t, new HashSet<Transaction>());
    			conflictingTx.get(t).addAll(utxoToListOfTransactions.get(u));
    			conflictingTx.get(t).remove(t);
    		}
    	}
    	
    	
    }
    
    
	Set<Transaction> txKeySet = conflictingTx.keySet();
	
	for(Transaction u : txKeySet)
	{
		System.err.println("Conflicts in "+u+" are "+conflictingTx.get(u));
		
	}   
	

    for(Transaction t : possibleTxs)
    {
    	int myFla=0;
    	dependencyMapList.put(t, new HashSet<Transaction>());
    	for(Transaction q : possibleTxs)
    	{
    		if(dependencyGraph.get(q).contains(t))
    		{
    			myFla=1;
    			dependencyMapList.get(t).add(q);
    		}
    	}
    	if(myFla==1)
    	{
    		dependencyMap.put(t,true);
    	}
    	else
    	{
    		dependencyMap.put(t,false);
    		if(conflictingTx.get(t).isEmpty()==true)
    		{
    			possibleTxsSet.remove(t);
    			emptySolutionSubSet.add(t);
    		}
    	}
    }

    System.err.println("Before Subset:"+emptySolutionSubSet);
    
    subsetConstructor(createCopyOfUtxoPool(publicLedger), emptySolutionSubSet,possibleTxsSet,oldemptySolutionSubSet);
    System.err.println("Subsets:"+arrayOfSubSets);
    
    int numberOfSubsets = globalcounter;
    globalcounter=0;
    
    ReturnValues finalValue = new ReturnValues(-1,null);
	ArrayList<Transaction> newList = new ArrayList<Transaction>();
	
	System.err.println("TopoOrder"+numberOfSubsets);
	System.err.println("TOPOSORT:"+topoOrder);
    for(int localcounter=0;localcounter<numberOfSubsets;localcounter++)
    {
    	globalcounter=localcounter;
    	if(arrayOfSubSets.get(localcounter).size()<=0)
    		continue;
    	findTopoSort();
        ArrayList<Transaction> maximalSetOfValidTransaction = new ArrayList<>();
        double profitValue=0;
        UTXOPool copyOfLedger = createCopyOfUtxoPool(publicLedger);
    	for(Transaction t : topoOrder)
    	{
    		if(isValidWithPool(t,copyOfLedger))
    		{
    			maximalSetOfValidTransaction.add(t);
    			profitValue+=profitCalculator(t,copyOfLedger);
    			for(int i=0;i<t.numInputs();i++)
    	        	{
    					copyOfLedger.removeUTXO(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
    	        	}
    		
    			for(int i=0;i<t.numOutputs();i++)
    	        	{
    	               	copyOfLedger.addUTXO(new UTXO(t.getHash(),i),t.getOutput(i));
    	        	}	
    			
    		}

    	}	
    	if(finalValue.getFee()<profitValue)
    	{ 
    		newList.clear();
    		newList.addAll(maximalSetOfValidTransaction);
    		finalValue = new ReturnValues(profitValue, newList);
    	}
    	topoOrder.clear();
    }
    
    System.err.println("Profit:"+finalValue.returnFee);
    ArrayList<Transaction> finalAnswer = finalValue.returnTransactionList;
    Transaction profitArray[] = new Transaction[finalAnswer.size()];
    for(int i=0; i<finalAnswer.size();i++)
    {
    	profitArray[i]=finalAnswer.get(i);
    }
    System.err.println("Ending");
    return profitArray;
    }

}
