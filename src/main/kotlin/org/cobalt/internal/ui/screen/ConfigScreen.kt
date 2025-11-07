package org.cobalt.internal.ui.screen

import net.minecraft.client.gui.Click
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.cobalt.Cobalt.mc
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.module.Category
import org.cobalt.api.module.ModuleManager
import org.cobalt.api.util.TickScheduler
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.helper.Config
import org.cobalt.internal.ui.animation.EaseInOutAnimation
import org.cobalt.internal.ui.animation.EaseOutAnimation
import org.cobalt.internal.ui.component.impl.CategoryComponent
import org.cobalt.internal.ui.component.impl.ModuleComponent
import org.cobalt.internal.ui.component.impl.SearchbarComponent
import org.cobalt.internal.ui.component.impl.SettingComponent
import org.cobalt.internal.ui.util.Constants
import org.cobalt.internal.ui.util.ScrollHandler
import org.cobalt.internal.ui.util.isHoveringOver

internal object ConfigScreen : Screen(Text.empty()) {

  /** Needed for opening animation */
  private val openAnim = EaseInOutAnimation(400)
  private var wasClosed = true

  /** Slide animation for the title in top bar */
  private val slideAnimation = EaseOutAnimation(200)
  private var isInModuleView = false

  private val categories = mutableListOf<CategoryComponent>()
  private val modules = mutableListOf<ModuleComponent>()
  private val settings = mutableListOf<SettingComponent>()

  private var selectedCategory: Category? = ModuleManager.getCategories().firstOrNull()
  private var selectedModule: ModuleComponent? = null

  private val categoryScroll = ScrollHandler()
  private val moduleScroll = ScrollHandler()
  private val settingScroll = ScrollHandler()

  fun setSelectedCategory(category: Category) {
    if (category == selectedCategory) return

    selectedCategory = category
    selectedModule = null

    updateRenderedModules()
    moduleScroll.reset()
    settingScroll.reset()

    if (isInModuleView) {
      isInModuleView = false
      slideAnimation.start()
    }
  }

  fun setSelectedModule(module: ModuleComponent) {
    if (module == selectedModule) return

    settings.clear()
    settings.addAll(module.getModule().getSettings().map(::SettingComponent))

    println(module.getModule().getSettings().size)

    selectedModule = module
    selectedCategory = null

    moduleScroll.reset()
    settingScroll.reset()

    if (!isInModuleView) {
      isInModuleView = true
      slideAnimation.start()
    }
  }

  fun updateRenderedModules() {
    modules.clear()
    modules.addAll(
      ModuleManager.getModules()
        .filter { it.category == selectedCategory }
        .map { ModuleComponent(it) }
    )
  }

  init {
    EventBus.register(this)
    categories.addAll(ModuleManager.getCategories().map(::CategoryComponent))
    updateRenderedModules()
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {
    if (mc.currentScreen != this)
      return

    val width = mc.window.width.toFloat()
    val height = mc.window.height.toFloat()

    val (startX, startY) = getStartCoords(width, height)

    categoryScroll.setMaxScroll(categories.size * 40f, Constants.SIDEBAR_HEIGHT - 80f)
    moduleScroll.setMaxScroll((modules.size + 2f) / 3f * (Constants.MODULE_HEIGHT + 15f), Constants.BODY_HEIGHT)

    selectedModule?.let {
      settingScroll.setMaxScroll(
        (it.getModule().getSettings().size) * (Constants.SETTING_HEIGHT + 15f),
        Constants.BODY_HEIGHT
      )
    }

    NVGRenderer.beginFrame(width, height)

    if (openAnim.isAnimating()) {
      val scale = openAnim.get(0f, 1f)
      NVGRenderer.translate(width / 2, height / 2)
      NVGRenderer.scale(scale, scale)
      NVGRenderer.translate(-width / 2, -height / 2)
    }

    drawSidebar(startX, startY)
    drawTopbar(startX, startY)
    drawBody(startX, startY)

    NVGRenderer.endFrame()
  }

  private fun drawSidebar(startX: Float, startY: Float) {
    NVGRenderer.rect(
      startX, startY,
      Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb, 4F
    )

    NVGRenderer.textWidth("cb", 20F).let {
      NVGRenderer.text(
        "cb",
        startX + (Constants.SIDEBAR_WIDTH / 2) - (it / 2),
        startY + 20F,
        20F, Constants.COLOR_WHITE.rgb
      )

      NVGRenderer.line(
        startX + (Constants.SIDEBAR_WIDTH / 2) - (it / 2),
        startY + 60F,
        startX + (Constants.SIDEBAR_WIDTH / 2) + (it / 2),
        startY + 60F,
        1F, Constants.COLOR_BORDER.rgb
      )
    }

    NVGRenderer.pushScissor(
      startX, startY + 80f,
      Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_HEIGHT - 80f
    )

    for ((index, category) in categories.withIndex()) {
      category.draw(
        startX + (Constants.SIDEBAR_WIDTH / 2) - 10f,
        startY + 80f + index * 40f - categoryScroll.getOffset(),
        selectedCategory
      )
    }

    NVGRenderer.popScissor()
  }

  private fun drawTopbar(startX: Float, startY: Float) {
    NVGRenderer.rect(
      startX + Constants.SIDEBAR_WIDTH + 3F,
      startY,
      Constants.TOPBAR_WIDTH, Constants.TOPBAR_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb, 4F
    )

    val textX = if (isInModuleView) {
      if (slideAnimation.isAnimating())
        slideAnimation.get(30.5F, 53F)
      else 53F
    } else {
      if (slideAnimation.isAnimating())
        slideAnimation.get(53F, 30.5F)
      else 30.5F
    }

    val title = selectedCategory?.name ?: selectedModule?.getModule()?.name ?: "No Selection"

    NVGRenderer.text(
      title,
      startX + Constants.SIDEBAR_WIDTH + textX,
      startY + (Constants.TOPBAR_HEIGHT / 2) - 7.5F,
      15F, Constants.COLOR_WHITE.rgb
    )

    selectedModule?.let {
      NVGRenderer.rect(
        startX + Constants.SIDEBAR_WIDTH + 18F,
        startY + (Constants.TOPBAR_HEIGHT / 2) - 12.5F,
        25F, 25F, Constants.COLOR_SURFACE.rgb, 4F
      )

      NVGRenderer.hollowRect(
        startX + Constants.SIDEBAR_WIDTH + 18F,
        startY + (Constants.TOPBAR_HEIGHT / 2) - 12.5F,
        25F, 25F, 1F, Constants.COLOR_BORDER.rgb, 4F
      )

      NVGRenderer.image(
        backCaretImage,
        startX + Constants.SIDEBAR_WIDTH + 27F,
        startY + (Constants.TOPBAR_HEIGHT / 2) - 6F,
        7F, 12F,
        colorMask = Constants.COLOR_WHITE.rgb
      )
    }

    SearchbarComponent.draw(
      startX + Constants.SIDEBAR_WIDTH + Constants.TOPBAR_WIDTH - Constants.SEARCHBAR_WIDTH - 15F,
      startY + (Constants.TOPBAR_HEIGHT / 2) - (Constants.SEARCHBAR_HEIGHT / 2)
    )
  }

  private fun drawBody(startX: Float, startY: Float) {
    NVGRenderer.rect(
      startX + Constants.SIDEBAR_WIDTH + 3F,
      startY + Constants.TOPBAR_HEIGHT + 3F,
      Constants.BODY_WIDTH, Constants.BODY_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb, 4F
    )

    NVGRenderer.pushScissor(
      startX + Constants.SIDEBAR_WIDTH + 3f,
      startY + Constants.TOPBAR_HEIGHT + 3f,
      Constants.BODY_WIDTH, Constants.BODY_HEIGHT
    )

    if (selectedModule == null) {
      for ((index, module) in modules.withIndex()) {
        val col = index % 3
        val row = index / 3
        module.draw(
          startX + Constants.SIDEBAR_WIDTH + 3f + 10f + col * (Constants.MODULE_WIDTH + 10f),
          startY + Constants.TOPBAR_HEIGHT + 15f + row * (Constants.MODULE_HEIGHT + 15f) - moduleScroll.getOffset()
        )
      }
    } else {
      for ((row, settings) in settings.withIndex()) {
        settings.draw(
          startX + Constants.SIDEBAR_WIDTH + 3f + 10f,
          startY + Constants.TOPBAR_HEIGHT + 15f + row * (Constants.SETTING_HEIGHT + 15f) - settingScroll.getOffset()
        )
      }
    }

    NVGRenderer.popScissor()
  }

  override fun mouseScrolled(
    amount: Double,
    mouseY: Double,
    horizontalAmount: Double,
    verticalAmount: Double
  ): Boolean {
    val (startX, startY) = getStartCoords()

    if (
      isHoveringOver(
        startX, startY + 80f,
        Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_HEIGHT - 80f
      )
    ) {
      categoryScroll.handleScroll(verticalAmount)
      return true
    }

    if (
      isHoveringOver(
        startX + Constants.SIDEBAR_WIDTH + 3f,
        startY + Constants.TOPBAR_HEIGHT + 3f,
        Constants.BODY_WIDTH, Constants.BODY_HEIGHT
      )
    ) {
      if (selectedModule == null)
        moduleScroll.handleScroll(verticalAmount)
      else
        settingScroll.handleScroll(verticalAmount)

      return true
    }

    return super.mouseScrolled(amount, mouseY, horizontalAmount, verticalAmount)
  }

  override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
    val (startX, startY) = getStartCoords()

    selectedModule?.let {
      if (
        isHoveringOver(
          startX + Constants.SIDEBAR_WIDTH + 20F,
          startY + (Constants.TOPBAR_HEIGHT / 2) - 17.5F,
          35F, 35F
        )
      ) {
        setSelectedCategory(it.getModule().category)
      }
    }

    for (module in modules) {
      if (module.onClick(click.button())) {
        return true
      }
    }

    for (category in categories) {
      if (category.onClick(click.button())) {
        return true
      }
    }

    return super.mouseClicked(click, doubled)
  }

  private fun getStartCoords(
    width: Float = mc.window.width.toFloat(),
    height: Float = mc.window.height.toFloat(),
  ): Pair<Float, Float> {
    return Pair(
      (width / 2) - (Constants.BASE_WIDTH / 2),
      (height / 2) - (Constants.BASE_HEIGHT / 2)
    )
  }

  override fun shouldPause(): Boolean {
    return false
  }

  override fun init() {
    if (wasClosed) {
      openAnim.start()
      wasClosed = false
    }

    super.init()
  }

  override fun close() {
    Config.saveModulesConfig()
    wasClosed = true
    super.close()
  }

  fun openUI() {
    TickScheduler.schedule(1) {
      mc.setScreen(this)
    }
  }

  val backCaretImage = NVGRenderer.createImage("/assets/cobalt/icons/caret_back.svg")

}
