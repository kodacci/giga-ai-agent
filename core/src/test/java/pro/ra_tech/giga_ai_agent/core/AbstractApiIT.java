package pro.ra_tech.giga_ai_agent.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
public abstract class AbstractApiIT implements CoreIT {
    @Autowired
    @Getter
    private RestTestClient restClient;
}
