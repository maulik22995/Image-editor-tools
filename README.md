
# CustomImageEditingView

CustomImageEditingView is an Android custom view designed to provide image editing functionalities like move, resize, flip, crop, skew, and opacity adjustment.

## Features

- Move the image within the view.
- Resize the image by dragging handles.
- Crop image to a specific area.
- Skew image horizontally or vertically (more control to stick any side)
- Adjust image opacity.
- Save edited image.

https://github.com/maulik22995/Image-editor-tools/assets/24403085/b7b20604-8247-4032-853b-0bdcd963f8e5

## Usage

**1. Add CustomImageEditingView to your layout XML:**

```
<com.image.editor.CustomImageEditingView
    android:id="@+id/customImageView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**2. Configure image editing modes:**

```
// Set mode to resize
customImageView.setMode(CustomImageEditingView.Mode.RESIZE)

// Set mode to crop
customImageView.setMode(CustomImageEditingView.Mode.CROP)

```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch (git checkout -b feature/new-feature).
3. Make your changes and commit them (git commit -am 'Add new feature').
4. push to the branch (git push origin feature/new-feature).
5. Create a pull request.



