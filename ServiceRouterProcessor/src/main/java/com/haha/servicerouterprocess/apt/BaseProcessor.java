package com.haha.servicerouterprocess.apt;

import com.haha.servicerouterannotation.annotation.utils.Consts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail:
 */
public abstract class BaseProcessor extends AbstractProcessor {

    Filer mFiler;
    Elements mElements;
    Types mTypes;
    String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mTypes = processingEnv.getTypeUtils();

        Map<String, String> options = processingEnv.getOptions();
        if (!options.isEmpty()) {
            moduleName = options.get(Consts.MODULE_NAME);
        }

        if (moduleName != null && !moduleName.isEmpty()) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]", "");
            Logger.info(">>> moduleName = " + moduleName + " <<<");
        } else {
            Logger.error(Consts.NO_MODULE_NAME_TIPS);
            throw new RuntimeException("DOFROUTER::Compiler >>> No module name, for more information, look at gradle log.");
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(Consts.MODULE_NAME);
        }};
    }

}
