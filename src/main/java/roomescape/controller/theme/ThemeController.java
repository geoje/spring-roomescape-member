package roomescape.controller.theme;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.service.ThemeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeService.getThemes());
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody final CreateThemeRequest createThemeRequest) {
        final ThemeResponse theme = themeService.addTheme(createThemeRequest);
        final URI uri = UriComponentsBuilder.fromPath("/themes/{id}")
                .buildAndExpand(theme.id())
                .toUri();

        return ResponseEntity.created(uri)
                .body(theme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable final Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularThemeResponse>> getPopularThemes(
            @RequestParam final int days,
            @RequestParam final int limit
    ) {
        return ResponseEntity.ok(themeService.getPopularThemes(new PopularThemeRequest(days, limit)));
    }
}
