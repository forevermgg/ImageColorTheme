#include <jni.h>
#include <string>
#include <colorquant.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <sys/stat.h>
#include <iostream>
#include <sstream>
#include <time.h>

#include <android/log.h>

#define TAG "ColorQuant"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,    TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,     TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,     TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,    TAG, __VA_ARGS__)

int file_size(std::string filename);
int run(std::string filename, int depth);
std::string rgb2hex(int r, int g, int b, bool with_head);

extern "C" JNIEXPORT jstring JNICALL
Java_com_mgg_quantcolor_ColorQuant_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

int run(std::string filename, int depth) {
    std::cout << filename << std::endl;
    int f_size = file_size(filename) / 3;
    if (depth == 4) {
        f_size = file_size(filename) / 4;
    }
    if (f_size <= 0) {
        LOGE("No File! Exiting...\n");
        return -1;
    }
    FILE *rgb_file = fopen(filename.c_str(), "rb");
    std::shared_ptr<PIX> pix = std::make_shared<PIX>();
    void *rgb_list;
    if (depth == 3) {
        rgb_list = malloc(f_size * sizeof(RGB_Quad));
        if (rgb_list == nullptr) {
            return -1;
        }
        fread(rgb_list, sizeof(RGB_Quad), f_size, rgb_file);
    } else {
        rgb_list = new RGBA_Quad[f_size];
        if (rgb_list == nullptr) {
            return -1;
        }
        fread(rgb_list, sizeof(RGBA_Quad), f_size, rgb_file);
    }
    if (pix == nullptr) {
        return -1;
    }
    pix->pixs = rgb_list;
    pix->depth = depth;
    pix->n = f_size;
    const int max_color = 9;
    const clock_t start = clock();
    std::shared_ptr<PIXCMAP> cmap = pix_median_cut_quant(pix, max_color, 5, 0);
    const clock_t end = clock();
    LOGE("time=%f ms\n", ((double)end - start) / CLOCKS_PER_SEC * 1000);
    if (cmap == nullptr) {
        return -1;
    }
    auto colorMapArray =
            cmap->array;
    for (auto &item : (*colorMapArray)) {
        LOGE("rgb(%d, %d, %d) %lu \n", item->red, item->green, item->blue,
               item->count);
        LOGE("rgb2hex %s\n", rgb2hex(item->red, item->green, item->blue, true).c_str());
    }
    fclose(rgb_file);
    free(pix->pixs);
    return 0;
}

int file_size(std::string filename) {
    struct stat statbuf;
    stat(filename.c_str(), &statbuf);
    const int size = statbuf.st_size;
    return size;
}

std::string rgb2hex(int r, int g, int b, bool with_head)
{
    std::stringstream ss;
    if (with_head)
        ss << "#";
    ss << std::hex << (r << 16 | g << 8 | b );
    return ss.str();
}

inline std::string JavaStringToStdString(JNIEnv* env, jstring jstr){
    if(!jstr)
        return "";
    const char* jchars=env->GetStringUTFChars(jstr, NULL);
    std::string str(jchars);
    env->ReleaseStringUTFChars(jstr, jchars);
    return str;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mgg_quantcolor_ColorQuant_colorQuant(JNIEnv *env, jclass clazz,
                                              jstring image_path) {
    run(JavaStringToStdString(env, image_path), 4);
}