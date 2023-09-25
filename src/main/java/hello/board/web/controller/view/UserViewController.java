package hello.board.web.controller.view;

import hello.board.dto.form.UserForm;
import hello.board.dto.form.UserForm.Save;
import hello.board.dto.service.UserServiceDto;
import hello.board.service.command.UserService;
import hello.board.web.annotation.ValidBinding;
import hello.board.web.annotation.ValidNewUser;
import hello.board.web.dtoresolver.UserServiceDtoResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserViewController {

    private final UserService userService;
    private final UserServiceDtoResolver dtoResolver;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", UserForm.Login.empty());
        return "login/loginForm";
    }

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("user", Save.empty());
        return "login/joinForm";
    }

    @PostMapping("/join")
    @ValidBinding(goBackTo = "login/joinForm")
    @ValidNewUser(goBackTo = "login/joinForm")
    public String join(@Valid @ModelAttribute("user") Save saveForm, BindingResult br) {
        UserServiceDto.Save param = dtoResolver.toSaveDto(saveForm);
        userService.save(param);
        return "redirect:/login";
    }

}
