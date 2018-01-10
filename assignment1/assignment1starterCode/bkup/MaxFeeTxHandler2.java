import java.util.*;

public class MaxFeeTxHandler 
{
	UTXOPool publicLedger,doubleSpendCheck;
	Map<Transaction,Double> transactionFee = new HashMap<Transaction,Double>();
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
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
                else    sumOfOutputs+=tx.getOutput(i).value;
        }

        if(sumOfInputs<sumOfOutputs)
                return false;
	if(transactionFee.containsKey(tx)==true)
		{}
	else transactionFee.put(tx,sumOfInputs-sumOfOutputs);
        return true;
    }

	double max=0,currentSum=0;
	ArrayList<Transaction> returnList = new ArrayList<Transaction>();

	public Transaction[] handleTxs(Transaction[] possibleTxs) 
	{
		if(possibleTxs.length==1)
		{
			if(isValidTx(possibleTxs[0]))
			{
				currentSum+=transactionFee.get(possibleTxs[0]);
				return possibleTxs;
			}
			return null;
		}
		else
		{
			for(int x=0;x<possibleTxs.length;x++)
			{
				if(isValidTx(possibleTxs[x]))
				{
					
						
					ArrayList<Transaction> remainingList = new ArrayList<Transaction>();
					ArrayList<Transaction> finalList = new ArrayList<Transaction>();
					currentSum+=transactionFee.get(possibleTxs[x]);
					for(int i=0;i<possibleTxs.length;i++)
					{
						if(i!=x)
						{
							remainingList.add(possibleTxs[i]);
						}
					}
					finalList.add(possibleTxs[x]);
					Transaction [] remainingArray = new Transaction[remainingList.size()];
					remainingList.toArray(remainingArray);
					Transaction[] testArray = handleTxs(remainingArray);					
					if(testArray!=null)
					{
						ArrayList<Transaction> appendList = new ArrayList<Transaction>(Arrays.asList(testArray));
						finalList.addAll(appendList);
					}
					if(max<currentSum)
					{
						returnList.clear();
						max=currentSum;
						returnList.addAll(finalList);
					}
					currentSum=0;


					//Set<Transaction> possiblemaxFeeTransactionSet = new HashSet<Transaction>(possibleTxs);
					//for(Transaction t : possiblemaxFeeTransactionSet)
					//{
					//	if(isValidTx(t))
					//	{
					//		currentSum+=transactionFee.get(t);						
					//	}
					//}
				}
			}
		}		
        	Transaction [] returnArray = new Transaction[returnList.size()];
	        return returnList.toArray(returnArray);
	}
	
}
