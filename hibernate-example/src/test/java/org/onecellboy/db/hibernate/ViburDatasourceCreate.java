package org.onecellboy.db.hibernate;

import org.vibur.dbcp.ViburDBCPDataSource;

public class ViburDatasourceCreate {
    public void viburCreate(){
        ViburDBCPDataSource dataSource = new ViburDBCPDataSource();

        dataSource.setJdbcUrl("jdbc:mysql://192.168.1.22:3306/WATERWORKS?useSSL=false&characterEncoding=utf-8&autoReconnect=true");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // dataSource.setUrl(datasourceUrl);
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        dataSource.setPoolInitialSize(30);
        dataSource.setPoolMaxSize(100);


        dataSource.setConnectionTimeoutInMs(15000);
        dataSource.setConnectionIdleLimitInSeconds(10);
        dataSource.setTestConnectionQuery("select 1");

        dataSource.setLogQueryExecutionLongerThanMs(500); //������ �ش�ð����� ��� �ʾ����� �α��ϴ� �ð�
        dataSource.setLogStackTraceForLongQueryExecution(true);// ������ ��� �ʾ����� �α��� ������
        dataSource.setStatementCacheMaxSize(200);//���� ĳ�� ������

        dataSource.start();


    }
}
