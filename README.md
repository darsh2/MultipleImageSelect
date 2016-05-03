#MultipleImageSelect

![Alt text](/screenshots/mis.gif)

An android library that allows selection of multiple images from gallery. It shows an initial
album (buckets) chooser and then images in selected album. Can limit the number of images that
can be selected. Can be used in apps with APK 11 onwards.

Sample app can be found [here](https://github.com/darsh2/MultipleImageSelect/tree/master/sample) 
#Usage
Include this library in your project using gradle (thanks to [JitPack.io](https://github.com/jitpack-io)).

For stable build:
```gradle
repositories {
  maven {
    url "https://jitpack.io"
  }
}

dependencies {
  compile 'com.github.darsh2:MultipleImageSelect:v0.0.4'
}
```

For using the latest build, replace the tag in dependencies above with latest commit hash. Example:
```gradle
repositories {
  maven {
    url "https://jitpack.io"
  }
}

dependencies {
  compile 'com.github.darsh2:MultipleImageSelect:3474549'
}
```

In project's AndroidManifest.xml, add the following under application node:
```xml
<activity
  android:name="com.darsh.multipleimageselect.activities.AlbumSelectActivity"
  android:theme="@style/MultipleImageSelectTheme">
  <intent-filter>
    <category android:name="android.intent.category.DEFAULT" />
  </intent-filter>
</activity>
```
   In the activity from where you want to call image selector, create Intent as follows:
```java
Intent intent = new Intent(this, AlbumSelectActivity.class);
//set limit on number of images that can be selected, default is 10
intent.putExtra(Constants.INTENT_EXTRA_LIMIT, numberOfImagesToSelect);
startActivityForResult(intent, Constants.REQUEST_CODE);
```
   and override onActivityResult as follows:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
    //The array list has the image paths of the selected images
    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
    ...  
}
```
#Custom Styles
![Alt text](/screenshots/misCC.gif)

1. To change the color of UI elements, in colors.xml file, override the following attributes with the desired colors. Example:

```xml
<color name="multiple_image_select_primary">#673AB7</color>
<color name="multiple_image_select_primaryDark">#512DA8</color>
<color name="multiple_image_select_primaryLight">#D1C4E9</color>
<color name="multiple_image_select_accent">#536DFE</color>
<color name="multiple_image_select_primaryText">#212121</color>
<color name="multiple_image_select_secondaryText">#727272</color>
<color name="multiple_image_select_divider">#B6B6B6</color>
<color name="multiple_image_select_toolbarPrimaryText">#FFFFFF</color>
<color name="multiple_image_select_albumTextBackground">#99FFFFFF</color>
<color name="multiple_image_select_imageSelectBackground">#000000</color>    
```

![Alt text](/screenshots/misCT.gif)

2. To change the theme altogether, do step 1, and make the following changes in styles.xml and manifest file:

styles.xml:
Create the theme you want to use along with a theme for toolbar and actionmode. Example:

```xml
<style name="OverrideMultipleImageSelectTheme" parent="Theme.AppCompat.NoActionBar">
  <item name="colorPrimary">@color/multiple_image_select_primary</item>
  <item name="colorPrimaryDark">@color/multiple_image_select_primaryDark</item>
  <item name="colorAccent">@color/multiple_image_select_accent</item>
  <item name="actionModeStyle">@style/OverrideCustomActionModeStyle</item>
  <item name="windowActionModeOverlay">true</item>
</style>
<style name="OverrideCustomActionModeStyle" parent="Base.Widget.AppCompat.ActionMode">
  <item name="background">@color/multiple_image_select_primary</item>
</style>
<style name="OverrideCustomToolbarTheme" parent="Base.ThemeOverlay.AppCompat.ActionBar">
</style>
```

AndroidManifest.xml:
Add ```tools:replace="android:theme"``` to AlbumSelectActivity and ImageSelectActivity and specify theme to use. Example: 

```xml
<manifest ...
  xmlns:tools="http://schemas.android.com/tools"
  ...>
  
  <activity android:name="com.darsh.multipleimageselect.activities.AlbumSelectActivity"
		tools:replace="android:theme"
    android:theme="@style/OverrideMultipleImageSelectTheme">
    <intent-filter>
      <category android:name="ANDROID.INTENT.CATEGORY.DEFAULT" />
    </intent-filter>
    </activity>
  <activity android:name="com.darsh.multipleimageselect.activities.ImageSelectActivity"
    tools:replace="android:theme"
    android:theme="@style/OverrideMultipleImageSelectTheme">
    <intent-filter>
      <category android:name="ANDROID.INTENT.CATEGORY.DEFAULT" />
    </intent-filter>
  </activity>
```
#Screenshots
Can be found [here](https://github.com/darsh2/MultipleImageSelect/tree/master/screenshots)
#Apps using this
[Gallery Organizer](https://play.google.com/store/apps/details?id=com.darsh.galleryorganizer2)

[PictureJam Collage Maker](https://play.google.com/store/apps/details?id=xyz.pichancer.picturejam.free)
#Similar Projects
Similar libraries can be found [here](https://android-arsenal.com/tag/157)
#Acknowledgements
This library makes use of [Glide](https://github.com/bumptech/glide) by Bump Technologies.
(Thanks to [WindSekirun](https://github.com/WindSekirun) for suggesting Glide)
#License
```license
Copyright 2015 Darshan Dorai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
