#include <android/log.h>
#include <colorquant.h>
#include <jni.h>
#include <sys/stat.h>

#include <codecvt>
#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <locale>
#include <sstream>
#include <string>

#define TAG "ColorQuant"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

int file_size(std::string filename);

[[maybe_unused]] int run(const std::string& filename, int depth);

std::string rgb2hex(int r, int g, int b, bool with_head);

int run(const std::string& filename, int depth, std::string& result) {
  std::cout << filename << std::endl;
  int f_size = file_size(filename) / 3;
  if (depth == 4) {
    f_size = file_size(filename) / 4;
  }
  if (f_size <= 0) {
    LOGE("No File! Exiting...\n");
    return -1;
  }
  FILE* rgb_file = fopen(filename.c_str(), "rb");
  std::shared_ptr<PIX> pix = std::make_shared<PIX>();
  void* rgb_list;
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
  auto colorMapArray = cmap->array;
  for (auto& item : (*colorMapArray)) {
    LOGE("rgb(%d, %d, %d) %zu \n", item->red, item->green, item->blue,
         item->count);
    LOGE("rgb2hex %s\n",
         rgb2hex(item->red, item->green, item->blue, true).c_str());
    result.append(rgb2hex(item->red, item->green, item->blue, true));
    result.append(",");
  }
  LOGE("result: %s\n", result.c_str());
  std::string blanks(",");
  result.erase(0, result.find_first_not_of(blanks));
  result.erase(result.find_last_not_of(blanks) + 1);
  LOGE("result: %s\n", result.c_str());
  fclose(rgb_file);
  free(pix->pixs);
  return 0;
}

int file_size(std::string filename) {
  struct stat statbuf{};
  stat(filename.c_str(), &statbuf);
  const int size = statbuf.st_size;
  return size;
}

std::string rgb2hex(int r, int g, int b, bool with_head) {
  std::stringstream ss;
  if (with_head) ss << "#";
  ss << std::hex << (r << 16 | g << 8 | b);
  return ss.str();
}

static std::string UTF16StringToUTF8String(const char16_t* chars, size_t len) {
  std::u16string u16_string(chars, len);
  return std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t>{}
      .to_bytes(u16_string);
}

std::string JavaStringToString(JNIEnv* env, jstring str) {
  if (env == nullptr || str == nullptr) {
    return "";
  }
  const jchar* chars = env->GetStringChars(str, nullptr);
  if (chars == nullptr) {
    return "";
  }
  std::string u8_string = UTF16StringToUTF8String(
      reinterpret_cast<const char16_t*>(chars), env->GetStringLength(str));
  env->ReleaseStringChars(str, chars);
  return u8_string;
}

static std::u16string UTF8StringToUTF16String(const std::string& string) {
  return std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t>{}
      .from_bytes(string);
}

jstring StringToJavaString(JNIEnv* env, const std::string& u8_string) {
  std::u16string u16_string = UTF8StringToUTF16String(u8_string);
  auto result = env->NewString(
      reinterpret_cast<const jchar*>(u16_string.data()), u16_string.length());
  return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_mgg_quantcolor_ColorQuant_colorQuant(JNIEnv* env, jclass clazz,
                                              jstring image_path) {
  std::string result;
  run(JavaStringToString(env, image_path), 4, result);
  return StringToJavaString(env, result);
}