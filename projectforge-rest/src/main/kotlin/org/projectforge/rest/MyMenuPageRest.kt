package org.projectforge.rest

import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.menu.Menu
import org.projectforge.menu.builder.FavoritesMenuCreator
import org.projectforge.menu.builder.MenuCreator
import org.projectforge.menu.builder.MenuCreatorContext
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 * For customization of the user's menu.
 */
@RestController
@RequestMapping("${Rest.URL}/myMenu")
class MyMenuPageRest : AbstractDynamicPageRest() {
  @Autowired
  private lateinit var favoritesMenuCreator: FavoritesMenuCreator

  @Autowired
  private lateinit var menuCreator: MenuCreator

  class MyMenu(
    var mainMenu: Menu,
    var favoritesMenu: Menu,
  )

  @GetMapping("dynamic")
  fun getForm(request: HttpServletRequest): FormLayoutData {
    val layout = UILayout("menu.customize.title")
    layout.add(UICustomized("menu.customization"))
    val menu = MyMenu(
      menuCreator.build(MenuCreatorContext(ThreadLocalUserContext.getUser())),
      favoritesMenuCreator.getFavoriteMenu(),
    )

    LayoutUtils.process(layout)

    layout.postProcessPageMenu()

    return FormLayoutData(menu, layout, createServerData(request))
  }

  @PostMapping
  fun save(request: HttpServletRequest, @RequestBody postData: PostData<MyMenu>)
      : ResponseEntity<ResponseAction> {
    validateCsrfToken(request, postData)?.let { return it }
    val data = postData.data
    return UIToast.createToastResponseEntity("OK");
  }
}
