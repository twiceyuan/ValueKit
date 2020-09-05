package com.twiceyuan.valuekit.registry

import com.twiceyuan.valuekit.ValueKitRegistry

/**
 * 默认 registry，无实现，作为默认配置会去获取 [Registry.getDefaultRegistry] 的值
 */
abstract class DefaultRegistry: ValueKitRegistry