package com.demo;

import com.android.build.gradle.AppExtension;
import com.demo.transform.LifeCycleTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LifeCyclePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("################ hello world");
        LifeCycleTransform transform = new LifeCycleTransform();
        project.getExtensions().getByType(AppExtension.class).registerTransform(transform);
    }
}
