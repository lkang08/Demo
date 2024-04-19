package com.demo.transform;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class LifeCycleTransform extends Transform {
    @Override
    public String getName() {
        return "LifeCycleTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException,
            InterruptedException, IOException {
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        inputs.forEach(input -> {
            input.getDirectoryInputs().forEach(directoryInput -> {
                File dir = directoryInput.getFile();
                if (dir != null) {

                }
            });
        });
    }
}
