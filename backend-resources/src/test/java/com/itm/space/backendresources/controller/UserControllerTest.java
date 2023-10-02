package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.Response;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user1", password = "user", authorities = "ROLE_MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {

    @MockBean
    private Keycloak keycloak;

    @Autowired
    private MockMvc mockMvc;

    @Value("${keycloak.realm}")
    private String realmItm;

    @Test
    @SneakyThrows
    public void hiMethodTest(){
        MockHttpServletResponse response = mvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("user1", response.getContentAsString());
    }

    @Test
    @SneakyThrows
    public void userCreatedTest() {
        RealmResource realmResourceMock = mock(RealmResource.class);
        UsersResource usersResourceMock = mock(UsersResource.class);
        UserRepresentation userRepresentationMock = mock(UserRepresentation.class);

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentationMock.getId()).thenReturn(UUID.randomUUID().toString());

        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                new UserRequest(
                        "yuri",
                        "yuri@mail.com",
                        "12345",
                        "Yuri",
                        "Yuriev")))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(keycloak).realm(realmItm);
        verify(realmResourceMock).users();
        verify(usersResourceMock).create(any(UserRepresentation.class));
    }

    @Test
    @SneakyThrows
    public void createUserInvalidRequestTest() {
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                new UserRequest(
                        "",
                        "bad@mail.com",
                        "bad",
                        "bad",
                        "bad")))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    @SneakyThrows
    public void getUserByIdTest(){
        UUID userId = UUID.randomUUID();
        RealmResource realmResourceMock = mock(RealmResource.class);
        UserResource userResourceMock = mock(UserResource.class);
        UserRepresentation userRepresentationMock = mock(UserRepresentation.class);

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(mock(UsersResource.class));
        when(realmResourceMock.users().get(eq(String.valueOf(userId)))).thenReturn(userResourceMock);
        when(userResourceMock.toRepresentation()).thenReturn(userRepresentationMock);
        when(userRepresentationMock.getId()).thenReturn(String.valueOf(userId));

        MockHttpServletResponse response = mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
    }


}
