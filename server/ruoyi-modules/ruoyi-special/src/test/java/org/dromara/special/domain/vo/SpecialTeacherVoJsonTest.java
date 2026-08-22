package org.dromara.special.domain.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialTeacherVoJsonTest {

    @Test
    void userIdSerializesAsJsonString() throws Exception {
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(1764000000000000008L);
        vo.setUserId(1764000000000000009L);
        vo.setOrgId(1764000000000000010L);
        vo.setResourceId(10L);
        vo.setName("李老师");

        String json = new ObjectMapper().writeValueAsString(vo);

        assertTrue(json.contains("\"userId\":\"1764000000000000009\""), json);
        assertTrue(json.contains("\"id\":\"1764000000000000008\""), json);
        assertTrue(json.contains("\"orgId\":\"1764000000000000010\""), json);
        assertTrue(json.contains("\"resourceId\":\"10\""), json);
    }
}
