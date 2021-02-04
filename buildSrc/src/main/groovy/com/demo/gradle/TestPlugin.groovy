package com.demo.gradle

import com.android.build.gradle.*
import com.demo.gradle.bytecode.MethodDurationTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        def androidExtension = project.extensions.getByName("android") as BaseExtension
        androidExtension.registerTransform(new MethodDurationTransform())

    }

}
