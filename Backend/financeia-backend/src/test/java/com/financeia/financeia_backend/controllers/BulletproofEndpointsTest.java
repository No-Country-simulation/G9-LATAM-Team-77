package com.financeia.financeia_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financeia.financeia_backend.dto.transaction.TransactionRequest;
import com.financeia.financeia_backend.service.DataScienceService;
import com.financeia.financeia_backend.service.AuthService;
import com.financeia.financeia_backend.service.DashboardService;
import com.financeia.financeia_backend.service.TransactionService;
import com.financeia.financeia_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BulletproofEndpointsTest {

    private MockMvc healthMockMvc;
    private MockMvc transactionMockMvc;
    private MockMvc authMockMvc;
    private MockMvc dashboardMockMvc;
    private MockMvc analisisMockMvc;
    private MockMvc userMockMvc;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuthService authService;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private DataScienceService dataScienceService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        healthMockMvc = MockMvcBuilders.standaloneSetup(new HealthController()).build();
        transactionMockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService)).build();
        authMockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
        dashboardMockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();
        analisisMockMvc = MockMvcBuilders.standaloneSetup(new AnalisisController(dataScienceService)).build();
        userMockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }

    @RepeatedTest(12)
    void healthEndpointStressTest() throws Exception {
        healthMockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @RepeatedTest(11)
    void getTransactionsStressTest() throws Exception {
        transactionMockMvc.perform(get("/api/v1/transactions"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void postTransactionsInvalidStressTest() throws Exception {
        TransactionRequest badRequest = new TransactionRequest(
                "",
                new BigDecimal("-10"),
                "Cat",
                "INVALID_TYPE",
                LocalDate.now().plusDays(10)
        );
        transactionMockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void authLoginStressTest() throws Exception {
        String badLogin = "{\"email\":\"invalid\",\"password\":\"\"}";
        authMockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void authRegisterStressTest() throws Exception {
        String badRegister = "{\"name\":\"\",\"email\":\"invalid\",\"password\":\"\"}";
        authMockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRegister))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void dashboardSummaryStressTest() throws Exception {
        dashboardMockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void analisisFinancieroStressTest() throws Exception {
        analisisMockMvc.perform(post("/api/v1/analisis-financiero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void getUserProfileStressTest() throws Exception {
        userMockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }

    @RepeatedTest(11)
    void putUserProfileStressTest() throws Exception {
        userMockMvc.perform(put("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 200 && status < 500;
                });
    }
}

