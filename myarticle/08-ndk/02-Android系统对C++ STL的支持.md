#### 背景

最近在移植某个第三方开源库到Android系统中，在我们的代码内调用了其接口，该接口定义如下：

```c++
namespace apollo {
namespace cyber {
std::unique_ptr<Node> CreateNode(const std::string& node_name,
                                 const std::string& name_space = "");
void CyberTest();

}  // namespace cyber
}  // namespace apollo
```

事先我已经将该第三方开源库通过NDK编译为对应的共享库，并在Android.bp以预编译库的形式进行了导入，之后在AOSP源码环境下进行编译。但是最终在链接阶段，却提示我该接口属于undefined symbol,其要求的symbol如下：

```c++
apollo::cyber::CreateNode(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&)
```

于是我使用nm命令查看该共享库中该接口的symbol：

```shell
nm -D libcyber.so | grep CreateNode | xargs c++filt
```

得到的symbol如下：

```
apollo::cyber::CreateNode(std::__ndk1::basic_string<char, std::__ndk1::char_traits<char>, std::__ndk1::allocator<char> > const&, std::__ndk1::basic_string<char, std::__ndk1::char_traits<char>, std::__ndk1::allocator<char> > const&)
```

经过对比发现链接时所需要的symbol与当前该共享库所提供的symbol确实无法对应。出现差异的地方属于C++标准库的内容。很明显，问题就出在这里：该共享库编译时所使用的C++ STL库与后续AOSP源码编译时的C++ STL库不相匹配。

#### Android STL

首先我们需要清楚，Android源码中是存在多种STL库的，以Android12为例，**主要包含三种STL库**：

**Android Runtime STL:**这是Android运行时系统库所依赖的STL库，**包含共享库libc++.so,静态库libc++_static.a**，其源码位于external/libcxx内，我们可以找到两者在Android.bp中的定义：

```C++
// host + device static lib
cc_library_static {
    name: "libc++_static",
    defaults: ["libc++ defaults"],
    vendor_available: true,
    product_available: true,
    ramdisk_available: true,
    vendor_ramdisk_available: true,
    recovery_available: true,
    apex_available: [
        "//apex_available:platform",
        "//apex_available:anyapex",
    ],
    // being part of updatable apexes, this should work on older releases
    min_sdk_version: "apex_inherit",
    native_bridge_supported: true,
    srcs: [
        "src/algorithm.cpp",
        "src/any.cpp",
        "src/bind.cpp",
        "src/charconv.cpp",
        "src/chrono.cpp",
        "src/condition_variable.cpp",
        "src/debug.cpp",
        "src/exception.cpp",
        "src/future.cpp",
        "src/hash.cpp",
        "src/ios.cpp",
        "src/iostream.cpp",
        "src/locale.cpp",
        "src/memory.cpp",
        "src/mutex.cpp",
        "src/new.cpp",
        "src/optional.cpp",
        "src/random.cpp",
        "src/regex.cpp",
        "src/shared_mutex.cpp",
        "src/stdexcept.cpp",
        "src/string.cpp",
        "src/strstream.cpp",
        "src/system_error.cpp",
        "src/thread.cpp",
        "src/typeinfo.cpp",
        "src/utility.cpp",
        "src/valarray.cpp",
        "src/variant.cpp",
        "src/vector.cpp",
    ],
    whole_static_libs: [
        "libc++abi",
    ],
    target: {
        windows: {
            srcs: [                                                                                                                                                                                         
                "src/support/win32/*.cpp",
            ]
        },
    },
}
// host + device dynamic lib 
cc_library_shared {
    name: "libc++",
    host_supported: true,
    vendor_available: true,
    product_available: true,
    native_bridge_supported: true,
    vndk: {
        enabled: true,
        support_system_process: true,
    },  
    ramdisk_available: true,
    vendor_ramdisk_available: true,
    recovery_available: true,
    apex_available: [
        "//apex_available:platform",
        "//apex_available:anyapex",
    ],  
    // being part of updatable apexes, this should work on older releases
    min_sdk_version: "apex_inherit",
    whole_static_libs: ["libc++_static"],
    stl: "none",

    pgo: {
        sampling: true,
    },  

    target: {
        android: {
            pgo: {
                profile_file: "libc++/libc++.profdata",
            },  
        },  
        darwin: {
            unexported_symbols_list: "lib/libc++unexp.exp",
            force_symbols_not_weak_list: "lib/notweak.exp",
            force_symbols_weak_list: "lib/weak.exp",
            ldflags: [
                "-Wl,-undefined,dynamic_lookup",
            ],  
        },  

        linux_bionic: {
            enabled: true,
        },
    },                                                                                                                                                                                                      
}
```

我们可以选择系统一些关键基础库来查看:

```shell
trout_x86:/ # ldd /system/lib/liblog.so                                                                                                                                                                                   
        linux-gate.so.1 => [vdso] (0xea103000)
        libc++.so => /system/lib/libc++.so (0xe9780000)
        libc.so => /apex/com.android.runtime/lib/bionic/libc.so (0xe9849000)
        libm.so => /apex/com.android.runtime/lib/bionic/libm.so (0xe9ec4000)
        libdl.so => /apex/com.android.runtime/lib/bionic/libdl.so (0xe9ea3000)
trout_x86:/ # 
trout_x86:/ # ldd /system/lib/libutils.so                                                                                                                                                                                 
        linux-gate.so.1 => [vdso] (0xee3c4000)
        libcutils.so => /system/lib/libcutils.so (0xed9e9000)
        liblog.so => /system/lib/liblog.so (0xee113000)
        libprocessgroup.so => /system/lib/libprocessgroup.so (0xed952000)
        libvndksupport.so => /system/lib/libvndksupport.so (0xee092000)
        libc++.so => /system/lib/libc++.so (0xed881000)
        libc.so => /apex/com.android.runtime/lib/bionic/libc.so (0xeda11000)
        libm.so => /apex/com.android.runtime/lib/bionic/libm.so (0xed7c5000)
        libdl.so => /apex/com.android.runtime/lib/bionic/libdl.so (0xed80a000)
        libbase.so => /system/lib/libbase.so (0xee0c0000)
        libcgrouprc.so => /system/lib/libcgrouprc.so (0xee05f000)
        libdl_android.so => /apex/com.android.runtime/lib/bionic/libdl_android.so (0xed85e000)
trout_x86:/ # 
```

无一例外这些系统基础库都是使用libc++.so.

**NDK STL:**这是Android NDK内预编译好的STL库，其源码位于prebuilts/ndk/current/sources/cxx-stl/llvm-libc++，包含**共享库libc++shared.so和静态库libc++_static.a**，其使用Android.mk进行定义：

```
include $(CLEAR_VARS)
LOCAL_MODULE := c++_static
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/../../../../NOTICE
LOCAL_SRC_FILES := $(libcxx_sources)
LOCAL_C_INCLUDES := $(libcxx_includes)
LOCAL_CPPFLAGS := $(libcxx_cxxflags) -ffunction-sections -fdata-sections
LOCAL_CPP_FEATURES := rtti exceptions
LOCAL_EXPORT_C_INCLUDES := $(libcxx_export_includes)
LOCAL_EXPORT_CPPFLAGS := $(libcxx_export_cxxflags)
LOCAL_EXPORT_LDFLAGS := $(libcxx_export_ldflags)
LOCAL_STATIC_LIBRARIES := libc++abi
LOCAL_ARM_NEON := false

ifeq ($(NDK_PLATFORM_NEEDS_ANDROID_SUPPORT),true)
    LOCAL_STATIC_LIBRARIES += android_support
endif

LOCAL_STATIC_LIBRARIES += libunwind
LOCAL_EXPORT_STATIC_LIBRARIES += libunwind
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := c++_shared
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/../../../../NOTICE
LOCAL_STRIP_MODE := none
LOCAL_SRC_FILES := $(libcxx_sources)
LOCAL_C_INCLUDES := $(libcxx_includes)
LOCAL_CPPFLAGS := $(libcxx_cxxflags) -fno-function-sections -fno-data-sections
LOCAL_CPP_FEATURES := rtti exceptions
LOCAL_WHOLE_STATIC_LIBRARIES := libc++abi
LOCAL_EXPORT_C_INCLUDES := $(libcxx_export_includes)
LOCAL_EXPORT_CPPFLAGS := $(libcxx_export_cxxflags)
LOCAL_EXPORT_LDFLAGS := $(libcxx_export_ldflags)
ifeq ($(NDK_PLATFORM_NEEDS_ANDROID_SUPPORT),true)
    LOCAL_STATIC_LIBRARIES := android_support
endif
```

关于这两种STL的联系与区别，这里简单例举一下：

1. 两者同为LLVM实现，不过NDK自带的STL属于全标准的C++库，支持rtti与exceptions；而Android Runtime中所使用的STL不支持rtti与exceptions；

2. NDK自带的STL 命名空间会增加`std::__ndk1`，而 Android Runtime的STL命名空间会增加 std::__1，这点很重要，是导致我们链接时出现undefined symbol的关键原因，我们这里可以举个例子，以libbase.so的中的接口ReadFileToString为例，函数原型为：

```c++
android::base::ReadFileToString(std::basic_string<char, std::char_traits<char>, std::allocator<char> > const&, std::basic_string<char, std::char_traits<char>, std::allocator<char> >*)
```

假如我们使用gnustl STL标准库进行编译，其得到的symbol表应该是这样的：

```
android::base::ReadFileToString(std::basic_string<char, std::char_traits<char>, std::allocator<char> > const&, std::basic_string<char, std::char_traits<char>, std::allocator<char> >*)
```

假如我们使用NDK自带的STL标准库进行编译，其得到的symbol表则是这样的：

```
android::base::ReadFileToString(std::__ndk1::basic_string<char, std::__ndk1::char_traits<char>, std::__ndk1::allocator<char> > const&, std::__ndk1::basic_string<char, std::__ndk1::char_traits<char>, std::__ndk1::allocator<char> >*)
```

假如我们使用Android Runtime自带的STL标准库进行编译，得到的symbol则是这样的：

```
android::base::ReadFileToString(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> >*)
```

这里我们也可以查看实际的symbol具体是什么样的：

```
bcd@bstcd-OptiPlex-7090:/mnt/disk/libraries$ nm -D libbase.so | grep ReadFileToString | xargs c++filt
00012990
T
android::base::ReadFileToString(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> >*, bool)
bcd@bstcd-OptiPlex-7090:/mnt/disk/libraries$ 
```

从这个symbol来看，libbase.so所使用的STL库即为Android Runtime自带的STL标准库：lic++.so，我们ldd看一下：

```shell
trout_x86:/ # ldd /system/lib/libbase.so                                               linux-gate.so.1 => [vdso] (0xf4a71000)
        liblog.so => /system/lib/liblog.so (0xf47aa000)
        libc++.so => /system/lib/libc++.so (0xf4084000)
        libc.so => /apex/com.android.runtime/lib/bionic/libc.so (0xf4153000)
        libm.so => /apex/com.android.runtime/lib/bionic/libm.so (0xf47c3000)
        libdl.so => /apex/com.android.runtime/lib/bionic/libdl.so (0xf4072000)
trout_x86:/ # 
```

印证无误。

**System STL**:除了上述两种STL库，Android内还有另外一种系统库，也就是由bionic提供的libstdc++，其源码位于/bionic/libc目录内，使用Android.bp进行定义，其编译产物为libstdc++.so与libstdc++.a，其中 libstdc++.so 位于编译后将被install至/system/lib目录：

```
// ========================================================
// libstdc++.so and libstdc++.a.
// ========================================================

cc_library {
    defaults: ["libc_defaults"],
    include_dirs: ["bionic/libstdc++/include"],
    srcs: [
        "bionic/__cxa_guard.cpp",
        "bionic/__cxa_pure_virtual.cpp",
        "bionic/new.cpp",
    ],   
    name: "libstdc++",
    static_ndk_lib: true,
    static_libs: ["libasync_safe"],

    static: {
        system_shared_libs: [],
    },   
    shared: {
        system_shared_libs: ["libc"],
    },

    //TODO (dimitry): This is to work around b/24465209. Remove after root cause is fixed
    arch: {
        arm: {
            // TODO: This is to work around b/24465209. Remove after root cause is fixed.
            pack_relocations: false,
            ldflags: ["-Wl,--hash-style=both"],
            version_script: ":libstdc++.arm.map",
        },
        arm64: {
            version_script: ":libstdc++.arm64.map",
        },
        x86: {
            pack_relocations: false,
            ldflags: ["-Wl,--hash-style=both"],
            version_script: ":libstdc++.x86.map",
        },
        x86_64: {
            version_script: ":libstdc++.x86_64.map",
        },
    },
}

genrule {
    name: "libstdc++.arm.map",
    out: ["libstdc++.arm.map"],
    srcs: ["libstdc++.map.txt"],
    tool_files: [":bionic-generate-version-script"],                                                                                                                                               
    cmd: "$(location :bionic-generate-version-script) arm $(in) $(out)",
}
```

System STL标准库支持基础的C++运行时ABI，根本上而言也就是new和delete，该库同样不支持异常处理和RTTI。

这里再简单总结一下，Android系统内的STL支持从源头来讲分为两类，一类是跟随Android版本自身，也就是Android Runtime使用的libc++.so；另一类则是由NDK提供的，分为libc++*shared.so/libc++*_static.a以及libstdc++.so.

#### NDK 编译STL设定

我们已经知道了在Android源码内提供了多种STL支持，其中Android Runtime STL也就是libc++.so是AOSP默认缺省使用的STL，但在NDK开发中是无法使用该STL的，以当前最新的NDK版本r25b为例，提供的STL设定选项包括三种：libc++\system\none，在编译阶段使用名称指代时分为c++_shared,c++_static,none,或system，前两者都是libc++.

初次一看，相比大家都比较疑惑，libc++是不是就对应libc++.so呢，当然不是了。这里例举这三个选项所对应的具体的STL标准库：

libc++选项：包含共享库libc++*shared.so，静态库libc++*static.a.

system选项：对应于libstdc++.so.

none选项：不提供STL支持.

事实上在NDK r18之前，NDK中还提供了gnustl和stlport，在r18之后则不再提供，此时libc++.so属于AOSP源码编译时缺省的默认STL库。

在使用NDK编译工具链时，我们需要对STL进行设定，这里将结合不同的编译系统来说明如何进行设定。

**使用CMake：**

在使用CMake结合NDK编译时，具体如何编译可以参考我的[文章](https://coderfan.net/en/third-party-open-source-library-migrating-to-android.html/)。在默认情况下，CMake编译系统将会以c++*static作为STL库支持，但我们可以通过ANDROID*_STL变量来指定使用具体的STL支持，如下所示：

```
-DANDROID_STL=c++_shared 
-DANDROID_STL=c++_static
-DANDROID_STL=system
-DANDROID_STL=none
```

**使用NDK-Build：**

在使用NDK-Build编译时，默认不提供STL库支持，但我们如果需要指定STL支持，需要在Application.mk内通过APP_STL来进行指定，可选择与CMake一致，分别为c++_shared、c++_static、system与none.

#### 源码编译STL设定

如果我不需要使用NDK进行编译，而是直接在AOSP源码环境内编译时，该如何进行STL库的设定呢，这里我们将分别以Android.mk和Android.bp内的设定为例。

**Android.mk**：如果没有特殊设定，将会以libc++.so作为标准STL支持，如果我们需要使用另外两种，也就是NDK支持的libc++和system，需要进行额外设定，主要设定包括：

LOCAL_SDK_VERSION：指定SDK版本，也就是Android API级别.

LOCAL_NDK_STL_VARIANT:指定具体的STL支持，分为 c++_shared、c++_static、system与none.，也就是NDK提供的STL支持。

**Android.bp:**在Android.bp中指定STL库支持，需要分是否指定了sdk_version字段这两种情况。

**指定sdk_version时**：将会以NDK中提供的STL库作为支持，以stl字段作为标识，如下所示：

```
cc_library_shared{

   sdk_version:"current",
   stl: "libc++",
   //其中stl的可选值与对应关系：
   // ""              -> "ndk_system",libstdc++.so
   //"system"        -> "ndk_system",libstdc++.so
   //"c++_shared"    -> "ndk_libc++_shared",libc++_shared.so
   //"c++_static"    -> "ndk_libc++_static",libc++_static.a
   //"libc++"        -> "ndk_libc++_shared",libc++_shared.so
   //"libc++_static" -> "ndk_libc++_static",libc++_static.a
   //"none"          -> "",无

}
```

**未指定sdk_version时**：此时设定stl字段，会以Android Runtime中提供的STL库作为支持，如下所示：

```
cc_library_shared{

   stl: "libc++",
   //其中stl的可选值与对应关系：
   // ""             -> "libc++_static",libc++_static.a
   //"system"        -> "libc++_static",libc++_static.a
   //"c++_shared"    -> "libc++",libc++.so
   //"c++_static"    -> "libc++_static",libc++_static.a
   //"libc++"        -> "libc++",libc++.so
   //"libc++_static" -> "libc++_static",libc++_static.a
   //"none"          -> "",无

}
```

这里简单总结一下：

1.Android.mk中以LOCAL_NDK_STL_VARIANT指定NDK里的STL；

2.Android.bp中以stl字段指定STL，以sdk_version字段是否指定来区分是NDK STL还是Android Runtime STL;

#### Others

关于NDK编译的使用场景，这里需要做进一步说明。NDK编译本身是为Android APP开发准备的，方便APP开发者使用C/C++的代码，而最终编译的库也是会一起打包到APK内。如果我们需要将编译的库用于Android系统，放置于/system/lib64或者/system/lib内，需要将源码放到AOSP Tree中进行编译，避免STL不匹配问题。关于这方面的具体信息，可以参考该[Issue](https://github.com/android/ndk/issues/744#issuecomment-436037520)，这一点是需要我们在移植之前就明确的。