package com.wallace.demo.app.utils.zkUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Author: biyu.huang
 * Date: 2023/2/1 18:44
 * Description:
 */
public class CuratorClient {
  public static String LEADER_PATH = "/leader";

  public static CuratorFramework getCuratorClient() {
    CuratorFramework curatorFramework = CuratorFrameworkFactory
        .builder()
        .connectString("localhost:2181")
        .sessionTimeoutMs(3000)
        .connectionTimeoutMs(3000)
        .retryPolicy(new ExponentialBackoffRetry(1000, 10))
        .namespace("base")
        .build();
    curatorFramework.start();
    return curatorFramework;
  }

  public static void main(String[] args) throws Exception {
    CuratorFramework client = getCuratorClient();

    client.create()
        .creatingParentsIfNeeded()
        .withMode(CreateMode.EPHEMERAL)
        .forPath(LEADER_PATH + "/test");
  }
}
