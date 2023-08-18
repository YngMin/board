package hello.board.web.controller.view;

import hello.board.dto.form.UserForm;
import hello.board.dto.form.UserForm.Save;
import hello.board.service.command.UserService;
import hello.board.service.query.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserViewController {

    private final UserQueryService userQueryService;
    private final UserService userService;

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
    public String join(@Valid @ModelAttribute("userSaveForm") Save userSaveForm,
                       BindingResult bindingResult)
    {

        if (bindingResult.hasErrors()) {
            return "login/joinForm";
        }

        if (userSaveForm.passwordDoesNotMatch()) {
            bindingResult.reject("PasswordNotMatch");
            return "login/joinForm";
        }

        if (userQueryService.existsByEmail(userSaveForm.getEmail())) {
            bindingResult.reject("EmailExists");
            return "login/joinForm";
        }

        try {
            userService.save(userSaveForm.toDto());
        } catch(DataIntegrityViolationException e) {
            bindingResult.reject("EmailExists");
            return "login/joinForm";
        }

        return "redirect:/login";
    }
}
