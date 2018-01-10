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
    

    public double profitCalculator(Transaction tx)
    {
    	double sumInputs = 0;
        double sumOutputs = 0;
        for (Transaction.Input in : tx.getInputs())
        {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!publicLedger.contains(utxo) || !isValidTx(tx)) continue;
            Transaction.Output txOutput = publicLedger.getTxOutput(utxo);
            sumInputs += txOutput.value;
        }
        for (Transaction.Output out : tx.getOutputs()) 
        {
            sumOutputs += out.value;
        }
        return sumInputs - sumOutputs;
    }
    
    
    LinkedList<Transaction> finalSolutionSet = new LinkedList<Transaction>();
    LinkedList<Transaction> listOfContestedTx = new LinkedList<Transaction>();
    HashMap<UTXO,LinkedList<Transaction>> utxoToListOfTransactions = new HashMap<UTXO,LinkedList<Transaction>>();
    
    double finalSolutionAnswer=0.0;

   

    public void recursiveProcedure(LinkedList<Transaction> possibleTxsList)
    {
    	Iterator<Transaction> iter = possibleTxsList.iterator();
        HashMap<Transaction, Boolean> validityMap = new HashMap<Transaction,Boolean>();
    	int flag=0;
    	while(iter.hasNext())
    	{
    		Transaction t = iter.next();
    		if(isValidTx(t))
    		{
    			flag=1;
    			validityMap.put(t,true);
    		}
    		else
    			validityMap.put(t,false);
    	}	
    	
    	if(flag==0)
    	{
    		return;
    	}
    	
    	else
    	{
    		iter = possibleTxsList.iterator();
    		while(iter.hasNext())
        	{
        		Transaction t = iter.next();
        		for(int i=0;i<t.numInputs();i++)
    			{
    				UTXO checkUtxo = new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex);
    				if(checkUtxo!=null)
    				{
	    				if(publicLedger.contains(checkUtxo))
	    				{
	    					if(utxoToListOfTransactions.get(checkUtxo)==null)
	    					{	
								utxoToListOfTransactions.put(checkUtxo, new LinkedList<Transaction>());
	    					}
	    					utxoToListOfTransactions.get(checkUtxo).add(t);
						
	    					
	    				}
    				}
    			}
    		}
    		
    		iter = possibleTxsList.iterator();
    		
    		while(iter.hasNext())
        	{
        		Transaction t = iter.next();
        		if(validityMap.get(t)==true)
    			{
    				int flag2=0;
    				for(int i=0;i<t.numInputs();i++)
    				{
    					if(utxoToListOfTransactions.get(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex)).size()!=1)
    						flag2=1;
    				}
    				if(flag2==0)
    				{
    					finalSolutionSet.add(t);
    					finalSolutionAnswer+=profitCalculator(t);
    					for(int i=0;i<t.numInputs();i++)
    		        	{
    						publicLedger.removeUTXO(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
    		        	}
    			
    					for(int i=0;i<t.numOutputs();i++)
    	            	{
    	                   	publicLedger.addUTXO(new UTXO(t.getHash(),i),t.getOutput(i));
    	            	}
    					
    					iter.remove();
    					recursiveProcedure(possibleTxsList);
    				}
    				else
    				{
    					listOfContestedTx.add(t);
    				}
    			}
    		}
    		return;
    	}
    	
    	
	
    }
    
    
    
    public class ReturnValues{
    	private double returnFee;
    	private LinkedList<Transaction> returnTransactionList;
    	
    	public ReturnValues(double returnFee, LinkedList<Transaction> returnTransactionList)
    	{
    		returnTransactionList = new LinkedList<Transaction>();
    		this.returnFee=returnFee;
    		this.returnTransactionList=returnTransactionList;
    	}
    	
    	public LinkedList<Transaction> getList()
    	{
    		return returnTransactionList;
    	}
    	
    	public double getFee()
    	{
    		return returnFee;
    	}
    	
    }
    
    public UTXOPool createCopyOfUtxoPool(UTXOPool originalUtxoPool)
    {
    	UTXOPool returnPool = new UTXOPool();
    	LinkedList<UTXO> copyUtxoList = new LinkedList<UTXO>();
    	copyUtxoList.addAll(originalUtxoPool.getAllUTXO());
    	for(UTXO u : copyUtxoList)
    	{
    		returnPool.addUTXO(u,originalUtxoPool.getTxOutput(u));
    	}
    	
    	return returnPool;
    }
    
    
    public ReturnValues anotherRecursion(UTXOPool copyOfUtxoPool,LinkedList<Transaction> reducedPossibleTxsList)
    {
    	double rMax=0.0;
    	Iterator<Transaction> iter = reducedPossibleTxsList.iterator();
    	LinkedList <Transaction> rList = new LinkedList<Transaction>();
    	iter = reducedPossibleTxsList.iterator();	
    	while(iter.hasNext())
    	{
    		Transaction t = iter.next();
    		UTXOPool newCopyOfUtxoPool;
    		LinkedList<Transaction> newReducedListOfPossibleTxs = new LinkedList<Transaction>();
    		newCopyOfUtxoPool=createCopyOfUtxoPool(copyOfUtxoPool);
    		newReducedListOfPossibleTxs.addAll(reducedPossibleTxsList);
    		if(isValidWithPool(t,newCopyOfUtxoPool)==false)
    			continue;
    		for(int i=0;i<t.numInputs();i++)
        	{
    			LinkedList <Transaction> rivalTransactions = utxoToListOfTransactions.get(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
				newCopyOfUtxoPool.removeUTXO(new UTXO(t.getInput(i).prevTxHash,t.getInput(i).outputIndex));
				Iterator<Transaction> itera = newReducedListOfPossibleTxs.iterator();
	    		for(Transaction tx : rivalTransactions)
	        	{
	    			if(itera.hasNext())
	        		{
	    				Transaction tp = itera.next();
	        			if(newReducedListOfPossibleTxs.contains(tx)==true)
	        				itera.remove();
	        		}
	        	}
        	}
	
			for(int i=0;i<t.numOutputs();i++)
			{
               	newCopyOfUtxoPool.addUTXO(new UTXO(t.getHash(),i),t.getOutput(i));
        	}
    	
			ReturnValues rvalue = anotherRecursion(newCopyOfUtxoPool,newReducedListOfPossibleTxs);
			if(rvalue.getFee()>rMax)
			{
				rMax=rvalue.getFee()+profitCalculator(t);
				rList=rvalue.getList();
				rList.add(t);
			}
			
    	}
    	return new ReturnValues(rMax, rList);
    	
    }
    
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	
    	LinkedList<Transaction> finalSolutionList = new LinkedList<Transaction>();
    	ReturnValues finalValues;
    	LinkedList<Transaction> llPossibleTxs = new LinkedList<Transaction>(Arrays.asList(possibleTxs));
    	recursiveProcedure(llPossibleTxs);
    	finalValues=anotherRecursion(createCopyOfUtxoPool(publicLedger),listOfContestedTx);
    	
    	finalSolutionList=finalValues.getList();
    	finalSolutionList.addAll(finalSolutionSet);
    	
    	Transaction [] maximalSetOfValidTransactionArray = new Transaction[finalSolutionList.size()]; 
		return finalSolutionList.toArray(maximalSetOfValidTransactionArray);
    
    }

}

