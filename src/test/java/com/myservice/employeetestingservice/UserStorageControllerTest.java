package com.myservice.employeetestingservice;


import com.myservice.employeetestingservice.controllers.UserStorageController;
import com.myservice.employeetestingservice.domain.User;
import com.myservice.employeetestingservice.domain.UserStorage;
import com.myservice.employeetestingservice.dto.UserStorageDTO;
import com.myservice.employeetestingservice.service.LogService;
import com.myservice.employeetestingservice.service.UserService;
import com.myservice.employeetestingservice.service.UserStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.ui.Model;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserStorageControllerTest {

    @InjectMocks
    private UserStorageController userStorageController;

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private LogService logService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
/*
    Этот тест проверяет метод getAllUserStorages() контроллера UserStorageController.
    Он выполняет следующие действия:

    - Создание мокированного объекта userStorages:
        - Создается пустое множество userStorages, которое будет возвращаться мокированным сервисом userStorageService при вызове метода getAllUserStoragesWithDefaultParent().

    - Мокирование поведения userStorageService:
        - Настраивается метод when(userStorageService.getAllUserStoragesWithDefaultParent()).thenReturn(userStorages), чтобы при вызове возвращалось заранее подготовленное пустое множество userStorages.

    - Вызов метода контроллера:
        - Метод контроллера getAllUserStorages() вызывается с поддельным пользователем new User() и мокированным объектом модели model.

    - Проверка вызовов model.addAttribute:
        - Проверяется, что в модель были добавлены атрибуты:
            userStorages — объект множества userStorages.
            userStoragesForChoice — то же самое множество userStorages.

    - Проверка возвращаемого значения:
        - Проверяется, что метод контроллера возвращает строку "userStoragesPage", что указывает на название шаблона для рендеринга страницы.

    Цель теста:
        - Тест гарантирует, что:
            - Метод getAllUserStorages() корректно взаимодействует с userStorageService.
            - Данные, полученные из сервиса, правильно добавляются в модель.
            - Метод возвращает правильное имя представления (userStoragesPage), используемого для отображения результата.

    Итог:
    Тест проверяет, что метод выполняет свою задачу по извлечению и добавлению списка организаций в модель, а затем возвращает нужный шаблон для отображения страницы.
*/
    @Test
    void testGetAllUserStorages() {
        Set<UserStorage> userStorages = new HashSet<>();
        when(userStorageService.getAllUserStoragesWithDefaultParent()).thenReturn(userStorages);

        String result = userStorageController.getAllUserStorages(new User(), model);

        verify(model).addAttribute("userStorages", userStorages);
        verify(model).addAttribute("userStoragesForChoice", userStorages);
        assertEquals("userStoragesPage", result);
    }

    @Test
    void testAddUsersStorage() throws Exception {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        UserStorage parentUserStorage = new UserStorage();
        when(userStorageService.determineWhichParentStorage(anyString(), anyString())).thenReturn(parentUserStorage);
        when(modelMapper.map(any(UserStorageDTO.class), eq(UserStorage.class))).thenReturn(new UserStorage());

        String result = userStorageController.addUsersStorage(
                userStorageDTO, model, "parentName", "1", new User());

        verify(userStorageService).addUserStorage(any(UserStorage.class), any(User.class), eq(parentUserStorage));
        assertEquals("redirect:/userStorage", result);
    }

    @Test
    void testUpdateUsersStorage() throws Exception {
        UserStorageDTO userStorageDTO = new UserStorageDTO();
        when(userStorageService.getAllUserStoragesWithDefaultParent()).thenReturn(new HashSet<>());

        String result = userStorageController.updateUsersStorage(
                "1", userStorageDTO, model, "parentName", new User());

        verify(userStorageService).updateUserStorage(any(UserStorage.class), any(User.class), any());
        assertEquals("redirect:/userStorage", result);
    }

    @Test
    void testDeleteUsersStorage() throws Exception {
        when(userStorageService.deleteUserStorage(anyLong(), any(User.class))).thenReturn("0");

        String result = userStorageController.deleteUsersStorage("1", new User());

        verify(userStorageService).deleteUserStorage(1L, new User());
        assertEquals("redirect:/userStorage/0", result);
    }
}

