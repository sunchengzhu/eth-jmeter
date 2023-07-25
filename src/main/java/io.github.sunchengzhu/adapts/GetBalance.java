package io.github.sunchengzhu.adapts;

import io.github.sunchengzhu.model.Account;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GetBalance extends Web3BasicRequest {

    private List<Account> accountList;
    private static AtomicInteger curBalanceCheckIdx = new AtomicInteger(0);
    private int currentIdx;
    private Account currentAccount;
    private static final int PRINT_INTERVAL = 200;

    @Override
    public Arguments getConfigArguments() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.Mnemonic, Constant.DEFAULT_MNEMONIC);
        arguments.addArgument(Constant.SIZE, "100");
        return arguments;
    }

    @Override
    public void setupOtherData(JavaSamplerContext context) {
        String mnstr = context.getParameter(Constant.Mnemonic);
        int size = context.getIntParameter(Constant.SIZE);
        this.accountList = SingletonService.getSingletonAccountList(mnstr, size);
    }

    @Override
    public void prepareRun(JavaSamplerContext context) {
        this.currentIdx = curBalanceCheckIdx.getAndAdd(1) % this.accountList.size();
        this.currentAccount = this.accountList.get(currentIdx);
    }

    @Override
    public boolean run(JavaSamplerContext context) {
        return checkBalance(this.web3j, this.currentIdx, this.currentAccount);
    }

    private synchronized boolean checkBalance(Web3j web3j, int index, Account account) {
        try {
            Credentials credentials = account.getCredentials();
            String address = credentials.getAddress();

            // validate the address
            if (WalletUtils.isValidAddress(address)) {
                // proceed with balance check only if the address is valid
                EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

                // try to decode the balance
                try {
                    BigInteger balanceInWei = ethGetBalance.getBalance();
                    if (index % PRINT_INTERVAL == 0) {
                        System.out.println("Account " + index + " : " + address + " has " + Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER) + " Ether");
                    }
                } catch (MessageDecodingException e) {
                    // print the raw response if a MessageDecodingException occurs
                    System.out.println("Raw response: " + ethGetBalance.getRawResponse());
                    throw e;
                }
                return true;
            } else {
                System.out.println("The address " + address + " is not a valid Ethereum address. Skipping the balance check for this address.");
                return false;
            }
        } catch (MessageDecodingException e) {
            System.out.println("MessageDecodingException occurred for account at index " + index + " with address: " + account.getCredentials().getAddress());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Other exception occurred: " + e.getMessage());
            return false;
        }
    }

}
