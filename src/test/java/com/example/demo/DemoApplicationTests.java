package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("로그인 get")
	@Test
	void loginGet() throws Exception {
		this.mockMvc.perform(get("/login")
				.with(anonymous()))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@DisplayName("로그인 post")
	@Test
	void loginPost() throws Exception {
		String content = objectMapper.writeValueAsString(new User("user", "1111"));
		this.mockMvc.perform(MockMvcRequestBuilders
				.post("/login")
				.content(objectMapper.writeValueAsString(content))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
				.andExpect(status().is3xxRedirection());
	}

	@DisplayName("main 페이지")
	@Test
	@WithMockUser(username = "user", password = "1111", roles = "USER")
	void main() throws Exception {
		this.mockMvc.perform(get("/"))
				.andDo(print())
				.andExpect(status().isOk());
	}
	@Data
	static class User {
		String username;
		String password;

		User(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

}
