import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
	
	List<Transaction> maximalSetOfValidTransaction = new ArrayList<>();
	for(Transaction t : possibleTxs)
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
