package net.wade.autotests.core;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@ComponentScan(basePackages = "net.wade.autotests")
@PropertySource("/autotests.properties")
@PropertySource("/environments/autotests_${environment}.properties")
public class CassandraUtils {

    @Autowired
    Environment environment;

    private static final Logger logger = LogManager.getLogger(CassandraUtils.class);
    private static final int MAX_CASSANDRA_REQUEST_TRIES = 1;
    private static final int MAX_CASSANDRA_REQUEST_WAIT = 5;


    private CqlSession session;

    public CassandraUtils() {
        try {
            this.session = CqlSession.builder().build();
        } catch (Exception e) {
            logger.error("Ошибка подключения к Cassandra. Попробуйте снова.", e);
            this.session = CqlSession.builder().build();
        }
    }

    public CassandraUtils(String[] contactPoints, String localDataCenter) {
        this(contactPoints, localDataCenter, null);
    }

    public CassandraUtils(String[] contactPoints, String localDataCenter, String keySpace) {
        CqlSessionBuilder builder = CqlSession.builder();
        builder.addContactPoints(Arrays.stream(contactPoints)
                .map(s -> new InetSocketAddress(s.split(":")[0], Integer.parseInt(s.split(":")[1])))
                .collect(Collectors.toList()));
        builder.withLocalDatacenter(localDataCenter);

        if (keySpace != null) {
            builder.withKeyspace(keySpace);
        }

        try {
            this.session = builder.build();
        } catch (Exception e) {
            logger.error("Ошибка подключения к Cassandra. Попробуйте снова.", e);
            this.session = builder.build();
        }
    }

    public CassandraUtils(String keySpace) {
        this.session = CqlSession.builder().withKeyspace(keySpace).build();
    }

    public CqlSession getSession() {
        return session;
    }

    public ResultSet executeQueryWithWait(String query, Object... valuesToBind) {
        BoundStatement boundStatement = session.prepare(query).bind(valuesToBind);

        logger.info(
                String.format(
                        "Выполняем запрос к Cassandra с количеством повторений %s и временем ожидания %s:\n%s\nС аргументами: %s",
                        MAX_CASSANDRA_REQUEST_TRIES, MAX_CASSANDRA_REQUEST_WAIT, query, Arrays.toString(valuesToBind)
                )
        );

        ResultSet resultSet = null;
        for (int i = 1; i <= MAX_CASSANDRA_REQUEST_TRIES; i++) {
            resultSet = session.execute(boundStatement);
            if (resultSet.getAvailableWithoutFetching() > 0) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(MAX_CASSANDRA_REQUEST_WAIT);
            } catch (InterruptedException e) {
                logger.info("Ошибка ожидания Cassandra.", e);
            }
        }
        return resultSet;
    }

    public ResultSet executeQuery(String query, Object... valuesToBind) {
        logger.info(String.format("Выполняем запрос к Cassandra:\n%s\nС аргументами: %s", query, Arrays.toString(valuesToBind)));
        BoundStatement boundStatement = session.prepare(query).bind(valuesToBind);
        return session.execute(boundStatement);
    }
}



