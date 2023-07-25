package io.github.sunchengzhu.adapts;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class GetBlockByNumber implements JavaSamplerClient {

    private static final AtomicInteger currentBlockNumber = new AtomicInteger(1);
    private Web3j web3j;
    private Instant start;
    private int maxRequestCount;

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.RPC_URL, "");
        arguments.addArgument(Constant.MAX_REQUEST_COUNT, "2000");
        return arguments;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        setupWeb3j(context.getParameter(Constant.RPC_URL));
        maxRequestCount = Integer.parseInt(context.getParameter(Constant.MAX_REQUEST_COUNT));
    }

    private void setupWeb3j(String rpcUrl) {
        web3j = Web3j.build(new HttpService(rpcUrl));
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        int requestCount = currentBlockNumber.get();
        if (requestCount == 1) {
            start = Instant.now();
        }

        SampleResult result = new SampleResult();
        result.sampleStart(); // Jmeter 开始计时
        boolean success = getBlockByNumber(this.web3j, currentBlockNumber.getAndIncrement());
        result.setSuccessful(success); // 是否成功
        result.sampleEnd(); // Jmeter 结束计时

        if (requestCount == maxRequestCount) {
            Instant end = Instant.now();
            double timeElapsed = Duration.between(start, end).toMillis() / 1000.0;
            System.out.printf("Time elapsed for %d requests: %.1f seconds\n", maxRequestCount, timeElapsed);
        }

        return result;
    }

    private synchronized boolean getBlockByNumber(Web3j web3j, int blockNumber) {
        try {
            EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), false).send().getBlock();

            // Do something with the block. For example, print its hash:
            System.out.println("Block " + blockNumber + " hash is " + block.getHash());

            // Let's just return true if we've reached this point without exceptions:
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred: " + e.getMessage());
            return false;
        }
    }
}
