package org.onecellboy.akka.test;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ConfigurationOverride {


    public void configurationOverride()
    {
        // ����Ʈ ��������(���ҽ� ���� ����)
        Config baseConfig = ConfigFactory.load("application.conf");

        // ��� ��������, ����Ʈ ���������� �����.
        // �ܺ� ������ ����̴�.
        Config load =ConfigFactory.parseFile(new File("application.conf")).withFallback(baseConfig).resolve();

        ActorSystem system = ActorSystem.create("ClusterSystem",load);
    }


}
