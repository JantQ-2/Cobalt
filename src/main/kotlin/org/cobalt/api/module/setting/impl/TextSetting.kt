package org.cobalt.api.module.setting.impl

import org.cobalt.api.module.setting.Setting

class TextSetting(
  name: String,
  description: String,
  defaultValue: String
) : Setting<String>(name, description, defaultValue)
