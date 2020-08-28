package monitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import monitor.domain.OSInfo;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Controller
public class MonitorController implements InitializingBean {

    private static final String CLUSTER = "172.20.140.111:2181,172.20.140.220:2181,172.20.140.28:2181";
    private static final String ROOT_PATH = "/my-manager";
    private static final String SERVER_PATH = ROOT_PATH + "/server";
    ZkClient zkClient;

    @RequestMapping("/list")
    public String list(Model model){
        List<OSInfo> items = new ArrayList<>();
        List<String> children = zkClient.getChildren(ROOT_PATH);
        for (String child : children) {
            String path = ROOT_PATH + "/" + child;
            System.out.println(path);
            OSInfo osInfo = convert(zkClient.readData(path));
            System.out.println(osInfo);
            items.add(osInfo);
        }
        model.addAttribute("items", items);
        return "list";
    }

    ObjectMapper mapper = new ObjectMapper();
    private OSInfo convert(String json) {
        try {
            return mapper.readValue(json, OSInfo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        zkClient = new ZkClient(CLUSTER);
    }
}
