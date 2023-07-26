package io.github.sunchengzhu.adapts;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class EthCall implements JavaSamplerClient {

    private static final AtomicInteger requestCounter = new AtomicInteger(0);
    private Web3j web3j;
    private Instant start;
    private int maxRequestCount;


    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.RPC_URL, "");
        arguments.addArgument(Constant.MAX_REQUEST_COUNT, "2000");
        arguments.addArgument(Constant.FROM, "0x3499932d7a1d1850253d6c66d830e3524bb3f2a7");
        arguments.addArgument(Constant.TO, "0x6701bf03c0483c844d231246bbbbd6e1851c7ab1");
        arguments.addArgument(Constant.PAYLOAD, "0x20965255");
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
        int requestCount = requestCounter.incrementAndGet();
        if (requestCount == 1) {
            start = Instant.now();
        }

        String from = context.getParameter(Constant.FROM);
        String to = context.getParameter(Constant.TO);
        String data = context.getParameter(Constant.PAYLOAD);


        SampleResult result = new SampleResult();
        result.sampleStart(); // Jmeter 开始计时
        boolean success = call(this.web3j, from, to, data);
        result.setSuccessful(success); // 是否成功
        result.sampleEnd(); // Jmeter 结束计时

        if (requestCount == maxRequestCount) {
            Instant end = Instant.now();
            double timeElapsed = Duration.between(start, end).toMillis() / 1000.0;
            System.out.printf("Time elapsed for %d requests: %.1f seconds\n", maxRequestCount, timeElapsed);
        }

        return result;
    }

    private synchronized boolean call(Web3j web3j, String from, String to, String data) {
        try {
            Transaction transaction = Transaction.createEthCallTransaction(from, to, data);
            String response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send().getValue();
            System.out.println("Request " + requestCounter.get() + ": Call response is " + response);

            // Let's just return true if we've reached this point without exceptions:
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred: " + e.getMessage());
            return false;
        }
    }
}
