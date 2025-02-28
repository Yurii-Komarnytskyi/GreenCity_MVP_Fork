package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.*;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.EcoNewsService;
import greencity.service.TagsService;
import greencity.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EcoNewsControllerTest {
    private static final String ecoNewsLink = "/econews";
    private static final String uploadImageLink = "/uploadImage";
    private MockMvc mockMvc;
    @InjectMocks
    private EcoNewsController ecoNewsController;
    @Mock
    private EcoNewsService ecoNewsService;
    @Mock
    private TagsService tagsService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ObjectMapper objectMapper;

    private Principal principal = getPrincipal();

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(ecoNewsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .build();
    }

    @Test
    void getThreeLastEcoNewsTest() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/newest"))
            .andExpect(status().isOk());

        verify(ecoNewsService).getThreeLastEcoNews();
    }

    @Test
    void uploadImageTest() throws Exception {
        MockMultipartFile image = new MockMultipartFile("data", "filename.txt",
            "text/plain", "some xml".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + uploadImageLink)
            .file(image)).andExpect(status().isCreated());
        verify(ecoNewsService).uploadImage(isNull());
    }

    @Test
    void uploadImageTest_CreatedStatus() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
            "image/png", "some-image-content".getBytes());

        when(ecoNewsService.uploadImage(any(MultipartFile.class))).thenReturn("image-path");

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImage")
            .file(image))
            .andExpect(status().isCreated());

        verify(ecoNewsService).uploadImage(any(MultipartFile.class));
    }

    @Test
    void uploadImageTest_BadRequest() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "", "text/plain", "".getBytes());

        doThrow(new BadRequestException("Invalid file"))
            .when(ecoNewsService).uploadImage(any(MultipartFile.class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImage")
            .file(image))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
            .andExpect(result -> assertEquals("Invalid file", result.getResolvedException().getMessage()));

        verify(ecoNewsService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    void uploadImageTest_Forbidden() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png", "image-content".getBytes());

        doThrow(new UserHasNoPermissionToAccessException("Access denied"))
            .when(ecoNewsService).uploadImage(any(MultipartFile.class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImage")
            .file(image))
            .andExpect(status().isForbidden())
            .andExpect(
                result -> assertTrue(result.getResolvedException() instanceof UserHasNoPermissionToAccessException))
            .andExpect(result -> assertEquals("Access denied", result.getResolvedException().getMessage()));

        verify(ecoNewsService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    void uploadImagesTest_CreatedStatus() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile("images", "image1.png",
            "image/png", "image1-content".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "image2.png",
            "image/png", "image2-content".getBytes());

        when(ecoNewsService.uploadImages(any(MultipartFile[].class))).thenReturn(new String[] {"path1", "path2"});

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImages")
            .file(image1)
            .file(image2))
            .andExpect(status().isCreated());

        verify(ecoNewsService).uploadImages(any(MultipartFile[].class));
    }

    @Test
    void uploadImagesTest_BadRequest() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "", "", "".getBytes());

        doThrow(new BadRequestException("Invalid files"))
            .when(ecoNewsService).uploadImages(any(MultipartFile[].class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImages")
            .file(image))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
            .andExpect(result -> assertEquals("Invalid files", result.getResolvedException().getMessage()));

        verify(ecoNewsService, times(1)).uploadImages(any(MultipartFile[].class));
    }

    @Test
    void uploadImagesTest_UnsupportedMediaType() throws Exception {
        MockMultipartFile image1 =
            new MockMultipartFile("images", "image1.txt", "text/plain", "some text content".getBytes());
        MockMultipartFile image2 =
            new MockMultipartFile("images", "image2.doc", "application/msword", "some document content".getBytes());

        doThrow(new UnsupportedMediaTypeException("Only PNG and JPEG images are supported"))
            .when(ecoNewsService).uploadImages(any(MultipartFile[].class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImages")
            .file(image1)
            .file(image2))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnsupportedMediaTypeException))
            .andExpect(result -> assertEquals("Only PNG and JPEG images are supported",
                result.getResolvedException().getMessage()));

        verify(ecoNewsService, times(1)).uploadImages(any(MultipartFile[].class));
    }

    @Test
    void uploadImagesTest_Unauthorized() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "image.png", "image/png", "image-content".getBytes());

        doThrow(new AuthenticationException("Unauthorized"))
            .when(ecoNewsService).uploadImages(any(MultipartFile[].class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImages")
            .file(image))
            .andExpect(status().isUnauthorized());

        verify(ecoNewsService, times(1)).uploadImages(any(MultipartFile[].class));
    }

    @Test
    void uploadImagesTest_Forbidden() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "image.png", "image/png", "image-content".getBytes());

        doThrow(new UserHasNoPermissionToAccessException("Access denied"))
            .when(ecoNewsService).uploadImages(any(MultipartFile[].class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(ecoNewsLink + "/uploadImages")
            .file(image))
            .andExpect(status().isForbidden());

        verify(ecoNewsService, times(1)).uploadImages(any(MultipartFile[].class));
    }

    @Test
    void saveTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Olivia.Johnson@gmail.com");
        String json = "{\n" +
            "\"title\": \"title\",\n" +
            " \"tags\": [\"news\"],\n" +
            " \"text\": \"content content content\", \n" +
            "\"source\": \"\",\n" +
            " \"image\": null\n" +
            "}";
        MockMultipartFile jsonFile =
            new MockMultipartFile("addEcoNewsDtoRequest", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(ecoNewsLink)
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        AddEcoNewsDtoRequest addEcoNewsDtoRequest = mapper.readValue(json, AddEcoNewsDtoRequest.class);

        verify(ecoNewsService)
            .saveEcoNews(eq(addEcoNewsDtoRequest), isNull(), eq("Olivia.Johnson@gmail.com"));
    }

    @Test
    void saveBadRequestTest() throws Exception {
        mockMvc.perform(post(ecoNewsLink)
            .content("{}")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getEcoNewsById() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/{id}", 1))
            .andExpect(status().isOk());

        verify(ecoNewsService).findDtoByIdAndLanguage(1L, "en");
    }

    @Test
    void getEcoNewsByUserTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get(ecoNewsLink + "/byUser")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).getAllPublishedNewsByUser(userVO);
    }

    @Test
    void findAllTest() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsLink + "?page=1"))
            .andExpect(status().isOk());

        verify(ecoNewsService).findGenericAll(pageable);
    }

    @Test
    @SneakyThrows
    void getEcoNewsByUserByPage() {
        int pageNumber = 1;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(ecoNewsLink + "/byUserPage?page=1&size=2"))
            .andExpect(status().isOk());

        verify(ecoNewsService).findAllByUser(null, pageable);
    }

    @Test
    void deleteTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(delete(ecoNewsLink + "/{econewsId}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).delete(1L, userVO);
    }

    @Test
    void deleteTest_BadRequest() throws Exception {
        Long invalidEcoNewsId = -1L;
        UserVO userVO = getUserVO();

        when(userService.findByEmail(eq(principal.getName()))).thenReturn(userVO);

        doThrow(new BadRequestException("Invalid EcoNews ID"))
            .when(ecoNewsService).delete(eq(invalidEcoNewsId), eq(userVO));

        mockMvc.perform(delete("/econews/{econewsId}", invalidEcoNewsId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
            .andExpect(result -> assertEquals("Invalid EcoNews ID", result.getResolvedException().getMessage()));

        verify(ecoNewsService, times(1)).delete(eq(invalidEcoNewsId), eq(userVO));
    }

    @Test
    void deleteTest_Forbidden() throws Exception {
        Long ecoNewsId = 1L;
        UserVO userVO = getUserVO();

        when(userService.findByEmail(eq(principal.getName()))).thenReturn(userVO);

        doThrow(new UserHasNoPermissionToAccessException("Access denied"))
            .when(ecoNewsService).delete(eq(ecoNewsId), eq(userVO));

        mockMvc.perform(delete("/econews/{econewsId}", ecoNewsId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(
                result -> assertTrue(result.getResolvedException() instanceof UserHasNoPermissionToAccessException))
            .andExpect(result -> assertEquals("Access denied", result.getResolvedException().getMessage()));

        verify(ecoNewsService, times(1)).delete(eq(ecoNewsId), eq(userVO));
    }

    @Test
    void getEcoNewsTest() throws Exception {
        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<String> tags = Collections.singletonList("eco");

        mockMvc.perform(get("/econews/tags?page=5&tags=eco"))
            .andExpect(status().isOk());

        verify(ecoNewsService).find(pageable, tags);
    }

    @Test
    void getEcoNewsEmptyTagsListTest() throws Exception {
        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get("/econews/tags?page=5"))
            .andExpect(status().isOk());

        verify(ecoNewsService).findGenericAll(pageable);
    }

    @Test
    void getThreeRecommendedEcoNewsTest() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/recommended?openedEcoNewsId=" + 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).getThreeRecommendedEcoNews(1L);
    }

    @Test
    void findAllEcoNewsTagsTest() throws Exception {
        String language = "en";
        mockMvc.perform(get(ecoNewsLink + "/tags/all?lang=" + language))
            .andExpect(status().isOk());

        verify(tagsService).findAllEcoNewsTags(language);
    }

    @Test
    void likeTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(post(ecoNewsLink + "/like?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).like(userVO, 1L);
    }

    @Test
    void dislikeTest() throws Exception {
        // given
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        // when
        mockMvc.perform(post(ecoNewsLink + "/dislike?id=1")
            .principal(principal))
            .andExpect(status().isOk());
        // then
        verify(ecoNewsService).dislike(userVO, 1L);

    }

    @Test
    void countLikesForEcoNewsTest() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/countLikes/{econewsId}", 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).countLikesForEcoNews(1L);
    }

    @Test
    void checkNewsIsLikedByUserTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get(ecoNewsLink + "/isLikedByUser?econewsId=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService).checkNewsIsLikedByUser(1L, userVO);
    }

    @Test
    void findAmountOfPublishedNews() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/count")
            .param("userId", "1"))
            .andExpect(status().isOk());

        verify(ecoNewsService).getAmountOfPublishedNewsByUserId(1L);
    }

    @Test
    void getContentAndSourceForEcoNewsById() throws Exception {
        mockMvc.perform(get(ecoNewsLink + "/contentAndSourceForEcoNews/{id}", 1L))
            .andExpect(status().isOk());

        verify(ecoNewsService).getContentAndSourceForEcoNewsById(1L);

    }

    @Test
    void getContentAndSourceForEcoNewsByIdNot_Found_Request() throws Exception {

        Mockito.when(ecoNewsService.getContentAndSourceForEcoNewsById(1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(ecoNewsLink + "/contentAndSourceForEcoNews/{id}", 1L))
            .andExpect(status().isNotFound());

        verify(ecoNewsService).getContentAndSourceForEcoNewsById(1L);
    }
}
