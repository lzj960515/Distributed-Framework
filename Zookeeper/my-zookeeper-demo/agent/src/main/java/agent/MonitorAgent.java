package agent;

import agent.domain.CPUMonitorCalc;
import agent.domain.OSInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.I0Itec.zkclient.ZkClient;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * agent
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MonitorAgent {

    private static final String CLUSTER = "172.20.140.111:2181,172.20.140.220:2181,172.20.140.28:2181";
    private static final String ROOT_PATH = "/my-manager";
    private static final String SERVER_PATH = ROOT_PATH + "/server";


    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("搜集服务信息插件启动中....");
        ZkClient zkClient = new ZkClient(CLUSTER);
        if (!zkClient.exists(ROOT_PATH)) {
            zkClient.createPersistent(ROOT_PATH);
        }
        //创建临时节点
        String nodePath = zkClient.createEphemeralSequential(SERVER_PATH, getOsInfo());
        //上报信息
        Thread thread = new Thread(()->{
            while(true){
                String osInfo = getOsInfo();
                zkClient.writeData(nodePath, osInfo);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"zk-monitor");

        thread.setDaemon(true);
        thread.start();
        System.out.println("搜集服务信息插件已启动");
    }

    private static String getOsInfo() {
        OSInfo bean = new OSInfo();
        bean.lastUpdateTime = System.currentTimeMillis();
        bean.ip = getLocalIp();
        bean.cpu = CPUMonitorCalc.getInstance().getProcessCpu();
        MemoryUsage memoryUsag = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        bean.usedMemorySize = memoryUsag.getUsed() / 1024 / 1024;
        bean.usableMemorySize = memoryUsag.getMax() / 1024 / 1024;
        bean.pid = ManagementFactory.getRuntimeMXBean().getName();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getLocalIp() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return addr.getHostAddress();
    }


}
