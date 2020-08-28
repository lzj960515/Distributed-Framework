package demo.quickstart;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 常用api测试
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class NormalApiTest {


    ZooKeeper zooKeeper;

    @Before
    public void before() throws IOException {
        String connectString = "172.20.140.111:2181";
        zooKeeper = new ZooKeeper(connectString, 30000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println(event.getPath());
                System.out.println(event.getState());
                System.out.println(event.getType());
                System.out.println(event.getWrapper());
                System.out.println(event.toString());
            }
        });
    }

    @Test
    public void createNode() throws KeeperException, InterruptedException {
        List<ACL> aclList = new ArrayList<>();
        int perm = ZooDefs.Perms.ALL;
        ACL acl = new ACL(perm, new Id("world","anyone"));
        aclList.add(acl);
        zooKeeper.create("/test","hello api".getBytes(), aclList, CreateMode.PERSISTENT);
    }

    @Test
    public void getData() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData("/test", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("监听到事件发生");
                System.out.println(event);
                try {
                    byte[] data1 = zooKeeper.getData(event.getPath(), this, null);
                    System.out.println(new String(data1));
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);
        System.out.println(new String(data));
        System.out.println(stat);
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void getChildren() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren("/earth", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("监听到事件发生");
                System.out.println(event);
                try {
                    List<String> children1 = zooKeeper.getChildren(event.getPath(), this);
                    children1.forEach(System.out::println);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        children.forEach(System.out::println);
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void setAcl() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        List<ACL> acl = zooKeeper.getACL("/test", stat);
        acl.forEach(System.out::println);
        System.out.println(stat.getAversion());

        zooKeeper.addAuthInfo("digest","lzj:123".getBytes());

        List<ACL>  newAclList = new ArrayList<>();
        ACL acl1 = new ACL();
        acl1.setId(new Id("auth", "lzj"));
        acl1.setPerms(ZooDefs.Perms.ALL);
        newAclList.add(acl1);

        Stat stat1 = zooKeeper.setACL("/test", newAclList, stat.getAversion());
        System.out.println(stat1.getAversion());

    }


}
