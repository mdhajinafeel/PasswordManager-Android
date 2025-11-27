package com.nprotech.passwordmanager.view.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.material.button.MaterialButton;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.helper.CryptoHelper;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.model.SettingItem;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.utils.SimpleDividerItemDecoration;
import com.nprotech.passwordmanager.view.activities.MainActivity;
import com.nprotech.passwordmanager.view.adapter.SettingsRecyclerAdapter;
import com.nprotech.passwordmanager.viewmodel.AuthViewModel;
import com.nprotech.passwordmanager.viewmodel.MasterViewModel;
import com.nprotech.passwordmanager.viewmodel.PasswordViewModel;
import com.nprotech.passwordmanager.work.SyncScheduler;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements SettingsRecyclerAdapter.OnSettingActionListener {

    private RecyclerView settingsListView;
    private List<SettingItem> settingItems;
    private SettingsRecyclerAdapter adapter;
    private AuthViewModel loginViewModel;
    private MasterViewModel masterViewModel;
    private PasswordViewModel passwordViewModel;

    public static SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        try {

            AppCompatImageView profileImage = view.findViewById(R.id.profileImage);
            AppCompatTextView tvName = view.findViewById(R.id.tvName);
            AppCompatTextView tvEmail = view.findViewById(R.id.tvEmail);
            settingsListView = view.findViewById(R.id.settingsListView);
            MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
            AppCompatTextView tvAppVersion = view.findViewById(R.id.tvAppVersion);

            loginViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            txtTitle.setText(getString(R.string.settings));

            tvName.setText(PreferenceManager.INSTANCE.getName());
            tvEmail.setText(PreferenceManager.INSTANCE.getEmail());

            // Create and set the image
            new Thread(() -> {
                Bitmap avatar = createInitialsBitmap(getInitials(PreferenceManager.INSTANCE.getName()), ContextCompat.getColor(requireContext(), R.color.colorLightBlue));
                requireActivity().runOnUiThread(() -> profileImage.setImageBitmap(avatar));
            }).start();

            //VERSION
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);

            String versionName = pInfo.versionName;
            long versionCode;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = pInfo.getLongVersionCode();
            } else {
                versionCode = pInfo.versionCode;
            }

            tvAppVersion.setText(getString(R.string.app_version, versionName, versionCode));

            loginViewModel.getProgressState().observe(getViewLifecycleOwner(), isProgress -> {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    if (isProgress) {
                        mainActivity.showProgress();  // call showProgress
                    } else {
                        mainActivity.hideProgress();  // call hideProgress
                    }
                }
            });

            masterViewModel.getProgressState().observe(getViewLifecycleOwner(), isProgress -> {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    if (isProgress) {
                        mainActivity.showProgress();  // call showProgress
                    } else {
                        mainActivity.hideProgress();  // call hideProgress
                    }
                }
            });

            //LOGOUT
            btnLogout.setOnClickListener(view1 -> performLogout());

            //FETCH SETTINGS
            settingsListView.post(this::fetchSettings);
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error onCreateView", e);
        }

        return view;
    }

    private void fetchSettings() {
        try {
            settingItems = new ArrayList<>();
            settingItems.add(new SettingItem(1, R.drawable.ic_fingerprint, getString(R.string.enable_biometric), true, false, false));
            settingItems.add(new SettingItem(2, R.drawable.ic_dark_theme, getString(R.string.enable_dark_theme), true, false, false));
            settingItems.add(new SettingItem(3, R.drawable.ic_download, getString(R.string.download_passwords), false, false, false));
            settingItems.add(new SettingItem(4, R.drawable.ic_timer, getString(R.string.download_sync_interval), false, false, true));

            if (PreferenceManager.INSTANCE.getSyncHours() == 0) {
                settingItems.add(new SettingItem(5, R.drawable.ic_sync, getString(R.string.synchronization), false, false, false));
            }

            settingItems.add(new SettingItem(6, R.drawable.ic_about, getString(R.string.about_app), false, false, false));

            adapter = new SettingsRecyclerAdapter(requireContext(), settingItems, this);
            settingsListView.setLayoutManager(new LinearLayoutManager(requireContext()));
            settingsListView.addItemDecoration(new SimpleDividerItemDecoration(requireContext()));
            settingsListView.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error fetchSettings", e);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            initials.append(Character.toUpperCase(part.charAt(0)));
        }
        return initials.toString();
    }

    private Bitmap createInitialsBitmap(String initials, int bgColor) {
        int size = 200; // Fixed bitmap size
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw background circle
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(bgColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint);

        // Configure text paint
        Paint textPaint = createTextPaint(initials, size);

        // ✅ Adjust baseline vertically (centering fix)
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float x = size / 2f;
        float y = (size / 2f) - (fm.ascent + fm.descent) / 2f;

        // ✅ Now draw the text (make sure initials not empty)
        if (initials != null && !initials.isEmpty()) {
            canvas.drawText(initials, x, y, textPaint);
        }

        return bitmap;
    }

    // Extracted method
    private Paint createTextPaint(String text, int canvasSize) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);

        // Dynamically adjust text size to fit inside the circle
        float textSize = canvasSize / 2f; // Start large
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(text);
        while (textWidth > canvasSize * 0.8f) { // leave 10% padding
            textSize -= 2f;
            paint.setTextSize(textSize);
            textWidth = paint.measureText(text);
        }

        return paint;
    }

    @Override
    public void onSettingClick(SettingItem item, int itemId, int itemValue, String downloadType) {
        if (item.getSettingId() == 3) {
            if (Objects.equals(downloadType, "Excel")) {
                createExcelFile();
            } else if (Objects.equals(downloadType, "PDF")) {
                createPdfFile();
            }
        } else if (item.getSettingId() == 4) {
            if (itemId > 0) {
                PreferenceManager.INSTANCE.setSyncId(itemId);
                PreferenceManager.INSTANCE.setSyncHours(itemValue);

                if (itemValue == 0) {
                    SyncScheduler.cancelHourlySync(requireContext());
                } else {
                    SyncScheduler.scheduleHourlySync(requireContext(), PreferenceManager.INSTANCE.getSyncHours());
                }

                // ✅ Dynamically add/remove "Synchronization" item
                toggleSyncItem(itemValue == 0);
            }
        } else if (item.getSettingId() == 5) {
            if (itemId == 0) {
                masterViewModel.masterDownload();

                masterViewModel.getErrorMessage().observe(this, s -> ErrorDialogFragment.newInstance(
                        masterViewModel.getErrorTitle().getValue(),
                        masterViewModel.getErrorMessage().getValue(),
                        false
                ).show(getChildFragmentManager(), "errorDialog"));
            }
        }
    }

    private void createExcelFile() {

        if (hasStoragePermission()) {
            requestStoragePermission();
            return;
        }

        List<PasswordModel> passwordLists = passwordViewModel.getPasswords();
        if (passwordLists == null || passwordLists.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_passwords_found), Toast.LENGTH_SHORT).show();
            return;
        }

        try (HSSFWorkbook workbook = new HSSFWorkbook()) {

            HSSFSheet sheet = workbook.createSheet(getString(R.string.passwords));

            // ------------------------------
            // STYLES
            // ------------------------------

            // Title Style (Row 0)
            HSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE1.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setBorderTop(BorderStyle.THIN);
            titleStyle.setBorderBottom(BorderStyle.THIN);
            titleStyle.setBorderLeft(BorderStyle.THIN);
            titleStyle.setBorderRight(BorderStyle.THIN);

            HSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 12);
            titleStyle.setFont(titleFont);

            // Header Style (Row 1)
            HSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            HSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 10);
            headerStyle.setFont(headerFont);

            // Row Style (Data Rows)
            HSSFCellStyle rowStyle = workbook.createCellStyle();
            rowStyle.setAlignment(HorizontalAlignment.LEFT);
            rowStyle.setBorderTop(BorderStyle.THIN);
            rowStyle.setBorderBottom(BorderStyle.THIN);
            rowStyle.setBorderLeft(BorderStyle.THIN);
            rowStyle.setBorderRight(BorderStyle.THIN);

            HSSFFont rowFont = workbook.createFont();
            rowFont.setFontName("Calibri");
            rowFont.setFontHeightInPoints((short) 10);
            rowStyle.setFont(rowFont);

            // Hyperlink style
            HSSFCellStyle hyperlinkStyle = workbook.createCellStyle();
            HSSFFont hyperlinkFont = workbook.createFont();
            hyperlinkFont.setUnderline(HSSFFont.U_SINGLE);
            hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
            hyperlinkFont.setFontName("Calibri");
            hyperlinkStyle.setFont(hyperlinkFont);
            hyperlinkStyle.setBorderTop(BorderStyle.THIN);
            hyperlinkStyle.setBorderBottom(BorderStyle.THIN);
            hyperlinkStyle.setBorderLeft(BorderStyle.THIN);
            hyperlinkStyle.setBorderRight(BorderStyle.THIN);

            // ------------------------------
            // TITLE ROW (ROW 0)
            // ------------------------------
            HSSFRow titleRow = sheet.createRow(0);
            HSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(getString(R.string.passwords));
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // ------------------------------
            // HEADER ROW (ROW 1)
            // ------------------------------
            HSSFRow headerRow = sheet.createRow(1);

            String[] headers = {
                    getString(R.string.application_website_name),
                    getString(R.string.username_email),
                    getString(R.string.password_pin),
                    getString(R.string.application_link),
                    getString(R.string.strength)
            };

            for (int i = 0; i < headers.length; i++) {
                HSSFCell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Set column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 30 * 256);
            }

            // ------------------------------
            // DATA ROWS
            // ------------------------------
            CreationHelper helper = workbook.getCreationHelper();

            for (int i = 0; i < passwordLists.size(); i++) {

                PasswordModel pm = passwordLists.get(i);
                HSSFRow row = sheet.createRow(i + 2);

                // Application Name
                HSSFCell name = row.createCell(0);
                name.setCellValue(pm.getApplicationName());
                name.setCellStyle(rowStyle);

                // Username
                HSSFCell user = row.createCell(1);
                user.setCellValue(pm.getUserName());
                user.setCellStyle(rowStyle);

                // Password
                HSSFCell pass = row.createCell(2);
                pass.setCellValue(CryptoHelper.decrypt(pm.getPassword()));
                pass.setCellStyle(rowStyle);

                // Application Link
                HSSFCell linkCell = row.createCell(3);
                if (pm.getApplicationLink() != null && !pm.getApplicationLink().isEmpty()) {
                    linkCell.setCellValue(pm.getApplicationLink());
                    HSSFHyperlink hyperlink = (HSSFHyperlink) helper.createHyperlink(HyperlinkType.URL);
                    hyperlink.setAddress(pm.getApplicationLink());
                    linkCell.setHyperlink(hyperlink);
                    linkCell.setCellStyle(hyperlinkStyle);
                } else {
                    linkCell.setCellValue("-");
                    linkCell.setCellStyle(rowStyle);
                }

                // Password Strength
                HSSFCell strength = row.createCell(4);
                strength.setCellValue(getStrengthText(pm.getPasswordStrength()));
                strength.setCellStyle(rowStyle);
            }

            // ------------------------------
            // SAVE FILE
            // ------------------------------
            // Folder path inside External Storage
            File folder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));

            // Create folder if it doesn't exist
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    Toast.makeText(requireContext(), getString(R.string.unable_to_create_folder), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // File name with date
            String fileName = getString(R.string.app_name) + "_" + CommonUtils.getCurrentDate() + ".xlsx";

            // Create file inside the folder
            File file = new File(folder, fileName);

            // Write file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                Toast.makeText(requireContext(), getString(R.string.excel_file_created), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error createExcelFile", e);
        }
    }

    private void createPdfFile() {

        if (hasStoragePermission()) {
            requestStoragePermission();
            return;
        }

        List<PasswordModel> passwordLists = passwordViewModel.getPasswords();
        if (passwordLists == null || passwordLists.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_passwords_found), Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            // ------------------------------
            // FOLDER CREATION
            // ------------------------------
            File folder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));

            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    Toast.makeText(requireContext(), getString(R.string.unable_to_create_folder), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // ------------------------------
            // FILE NAME
            // ------------------------------
            String fileName = getString(R.string.app_name) + "_" + CommonUtils.getCurrentDate() + ".pdf";
            File file = new File(folder, fileName);

            // ------------------------------
            // PDF DOCUMENT
            // ------------------------------
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // ------------------------------
            // FONTS
            // ------------------------------
            BaseColor headerBg = new BaseColor(173, 216, 230); // Light blue

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font rowFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            Font linkFont = new Font(Font.FontFamily.HELVETICA, 11, Font.UNDERLINE, BaseColor.BLUE);

            // ------------------------------
            // TITLE
            // ------------------------------
            Paragraph title = new Paragraph(getString(R.string.passwords), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // ------------------------------
            // TABLE (5 columns)
            // ------------------------------
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // Column widths (similar to Excel)
            table.setWidths(new float[]{3f, 3f, 3f, 4f, 2f});

            // ------------------------------
            // HEADER ROW
            // ------------------------------
            String[] headers = {
                    getString(R.string.application_website_name),
                    getString(R.string.username_email),
                    getString(R.string.password_pin),
                    getString(R.string.application_link),
                    getString(R.string.strength)
            };

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // ------------------------------
            // DATA ROWS
            // ------------------------------
            for (PasswordModel pm : passwordLists) {

                // App name
                table.addCell(createCell(pm.getApplicationName(), rowFont));

                // Username
                table.addCell(createCell(pm.getUserName(), rowFont));

                // Password (decrypt)
                table.addCell(createCell(
                        CryptoHelper.decrypt(pm.getPassword()),
                        rowFont
                ));

                // Hyperlink or "-"
                if (pm.getApplicationLink() != null && !pm.getApplicationLink().isEmpty()) {
                    Anchor link = new Anchor(pm.getApplicationLink(), linkFont);
                    link.setReference(pm.getApplicationLink());

                    PdfPCell linkCell = new PdfPCell();
                    linkCell.addElement(link);
                    linkCell.setPadding(8);
                    table.addCell(linkCell);
                } else {
                    table.addCell(createCell("-", rowFont));
                }

                // Strength
                table.addCell(createCell(getStrengthText(pm.getPasswordStrength()), rowFont));
            }

            // Add table to document
            document.add(table);

            document.close();

            Toast.makeText(requireContext(), getString(R.string.pdf_file_created), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error createPdfFile", e);
        }
    }

    @Override
    public void onSwitchToggle(SettingItem item, boolean isChecked, LabeledSwitch switchButton) {
        if (item.getSettingId() == 1) {
            if (isChecked) {
                enableBiometricAuth(switchButton);
            } else {
                disableBiometricAuth(switchButton);
            }
        } else if (item.getSettingId() == 2) {
            toggleDarkMode(isChecked, switchButton);
        }
    }

    //BIOMETRIC
    private void enableBiometricAuth(LabeledSwitch switchButton) {
        BiometricManager biometricManager = BiometricManager.from(requireContext());

        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPrompt(switchButton);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireContext(), getString(R.string.no_biometric_hardware_detected), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(requireContext(), getString(R.string.biometric_hardware_unavailable), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(requireContext(), getString(R.string.no_biometric_enrolled_on_this_device), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Toast.makeText(requireContext(), getString(R.string.security_update_required_to_use_biometrics), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Toast.makeText(requireContext(), getString(R.string.biometric_type_not_supported_on_this_device), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Toast.makeText(requireContext(), getString(R.string.cannot_determine_biometric_status), Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(requireContext(), getString(R.string.biometric_status_unknown), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showBiometricPrompt(LabeledSwitch switchButton) {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(requireContext(), R.string.biometric_enabled_successfully, Toast.LENGTH_SHORT).show();

                // Save preference
                PreferenceManager.INSTANCE.setBioMetric(true);

                switchButton.setOn(PreferenceManager.INSTANCE.getBioMetric());
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.enable_biometric_unlock)).setSubtitle(getString(R.string.biometric_subtitle)).setNegativeButtonText(getString(R.string.text_cancel)).build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void disableBiometricAuth(LabeledSwitch switchButton) {
        PreferenceManager.INSTANCE.setBioMetric(false);
        switchButton.setOn(PreferenceManager.INSTANCE.getBioMetric());
        Toast.makeText(requireContext(), getString(R.string.biometric_disabled), Toast.LENGTH_SHORT).show();
    }

    //DARK MODE
    private void toggleDarkMode(boolean enable, LabeledSwitch switchButton) {
        AppCompatDelegate.setDefaultNightMode(enable ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        // Save preference
        PreferenceManager.INSTANCE.setDarkMode(enable);
        switchButton.setOn(PreferenceManager.INSTANCE.getDarkMode());
        Toast.makeText(requireContext(), getString(R.string.dark_mode) + (enable ? getString(R.string.enabled) : getString(R.string.disabled)), Toast.LENGTH_SHORT).show();
    }

    //LOGOUT
    private void performLogout() {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            dialogHeader.setText(getString(R.string.logout));
            dialogBody.setText(getString(R.string.logout_confirmation));

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {

                loginViewModel.logout();

                loginViewModel.getLogout().observe(requireActivity(), aBoolean -> {
                    dialog.dismiss();
                    if (aBoolean) {
                        if (getActivity() instanceof BaseActivity) {

                            PreferenceManager.INSTANCE.clearLoginSession();
                            SyncScheduler.cancelHourlySync(requireContext());
                            masterViewModel.clearScheduler();

                            ((BaseActivity) getActivity()).performLogout();
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error performLogout", e);
        }
    }

    private void toggleSyncItem(boolean show) {
        if (adapter == null || settingItems == null) return;

        int syncItemIndex = -1;
        for (int i = 0; i < settingItems.size(); i++) {
            if (settingItems.get(i).getSettingId() == 5) {
                syncItemIndex = i;
                break;
            }
        }

        if (show && syncItemIndex == -1) {
            // Find where Sync Interval (ID = 4) is located
            int insertAfterIndex = -1;
            for (int i = 0; i < settingItems.size(); i++) {
                if (settingItems.get(i).getSettingId() == 4) {
                    insertAfterIndex = i;
                    break;
                }
            }

            // Create new item
            SettingItem syncItem = new SettingItem(5, R.drawable.ic_sync, getString(R.string.synchronization), false, false, false);

            if (insertAfterIndex != -1 && insertAfterIndex < settingItems.size() - 1) {
                // Insert right after Sync Interval
                settingItems.add(insertAfterIndex + 1, syncItem);
                adapter.notifyItemInserted(insertAfterIndex + 1);
            } else {
                // Fallback — append at the end (should rarely happen)
                settingItems.add(syncItem);
                adapter.notifyItemInserted(settingItems.size() - 1);
            }

        } else if (!show && syncItemIndex != -1) {
            // Remove if exists
            settingItems.remove(syncItemIndex);
            adapter.notifyItemRemoved(syncItemIndex);
        }
    }

    // -------------------- STORAGE PERMISSION CHECK (ALL ANDROID VERSIONS) --------------------
    private boolean hasStoragePermission() {
        boolean granted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            granted = Environment.isExternalStorageManager();
        } else {
            granted = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return !granted;
    }

    @SuppressWarnings("deprecation")
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            requestPermissions(
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    101
            );
        }
    }

    private String getStrengthText(int strength) {
        switch (strength) {
            case 1:
                return getString(R.string.weak);
            case 2:
                return getString(R.string.medium);
            case 3:
                return getString(R.string.strong);
            case 4:
                return getString(R.string.very_strong);
            default:
                return getString(R.string.weak);
        }
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        return cell;
    }
}