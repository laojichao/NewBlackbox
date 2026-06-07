package android.content.pm;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Stub implementation of Android's {@code PackageParser} class.
 *
 * <p>Responsible for parsing APK files on disk and constructing in-memory representations
 * of packages. This includes extracting manifest information (activities, services, providers,
 * receivers, permissions), signatures, and other metadata. Supports both monolithic single-APK
 * packages and cluster-style split APK packages.</p>
 *
 * <p>This stub provides the hidden API used internally by the Android framework and
 * exposes it for use in sandboxed or virtualized environments.</p>
 *
 * @see Package
 * @see PackageLite
 */
public class PackageParser {
    /** Parse flag indicating the package is a system package. */
    public final static int PARSE_IS_SYSTEM = 1;

    /** Parse flag enabling verbose logging during parsing. */
    public final static int PARSE_CHATTY = 1 << 1;

    /** Parse flag requiring the source file to be a valid APK. */
    public final static int PARSE_MUST_BE_APK = 1 << 2;

    /** Parse flag to ignore process declarations in the manifest. */
    public final static int PARSE_IGNORE_PROCESSES = 1 << 3;

    /** Parse flag indicating the package is forward-locked (DRM-protected). */
    public final static int PARSE_FORWARD_LOCK = 1 << 4;

    /** Parse flag indicating the package is installed on external storage. */
    public final static int PARSE_EXTERNAL_STORAGE = 1 << 5;

    /** Parse flag indicating the package resides in the system directory. */
    public final static int PARSE_IS_SYSTEM_DIR = 1 << 6;

    /** Parse flag indicating the package is a privileged system app. */
    public final static int PARSE_IS_PRIVILEGED = 1 << 7;

    /** Parse flag to collect certificate information during parsing. */
    public final static int PARSE_COLLECT_CERTIFICATES = 1 << 8;

    /** Parse flag indicating the package uses trusted overlay. */
    public final static int PARSE_TRUSTED_OVERLAY = 1 << 9;

    /**
     * Describes a new permission introduced in a specific SDK version. Used to track
     * permissions that were added after the initial platform release.
     */
    public static class NewPermissionInfo {
        /** The name of the new permission. */
        public final String name;

        /** The SDK version in which this permission was introduced. */
        public final int sdkVersion;

        /** The file version associated with this permission. */
        public final int fileVersion;

        /**
         * Constructs a new {@link NewPermissionInfo}.
         *
         * @param name        the permission name
         * @param sdkVersion  the SDK version that introduced this permission
         * @param fileVersion the file version number
         */
        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Describes a permission that was split into multiple finer-grained permissions
     * in a later SDK version.
     */
    public static class SplitPermissionInfo {
        /** The original "root" permission that was split. */
        public final String rootPerm;

        /** The array of new permissions that replaced the root permission. */
        public final String[] newPerms;

        /** The target SDK version at which the split takes effect. */
        public final int targetSdk;

        /**
         * Constructs a new {@link SplitPermissionInfo}.
         *
         * @param rootPerm  the original permission name that was split
         * @param newPerms  the array of new permission names
         * @param targetSdk the SDK version at which the split applies
         */
        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Pre-defined list of permissions that were introduced after the initial
     * platform release, along with the SDK versions they were added in.
     */
    public static final PackageParser.NewPermissionInfo[] NEW_PERMISSIONS = new PackageParser.NewPermissionInfo[]{
            new PackageParser.NewPermissionInfo(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.os.Build.VERSION_CODES.DONUT, 0),
            new PackageParser.NewPermissionInfo(android.Manifest.permission.READ_PHONE_STATE, android.os.Build.VERSION_CODES.DONUT, 0)
    };

    /**
     * Internal argument container for parsing a package item (activity, service, etc.)
     * from manifest XML resources.
     */
    static class ParsePackageItemArgs {
        /** The owning package being parsed. */
        final Package owner;

        /** Output array for error messages. */
        final String[] outError;

        /** Resource ID for the component name attribute. */
        final int nameRes;

        /** Resource ID for the label attribute. */
        final int labelRes;

        /** Resource ID for the icon attribute. */
        final int iconRes;

        /** Resource ID for the logo attribute. */
        final int logoRes;

        /** Resource ID for the banner attribute. */
        final int bannerRes;

        /** Tag name for the XML element being parsed. */
        String tag;

        /** Typed array of attributes from the XML element. */
        TypedArray sa;

        /**
         * Constructs parse arguments for a package item.
         *
         * @param owner      the owning {@link Package}
         * @param outError   output array for error messages
         * @param nameRes    resource ID for the name attribute
         * @param labelRes   resource ID for the label attribute
         * @param iconRes    resource ID for the icon attribute
         * @param logoRes    resource ID for the logo attribute
         * @param bannerRes  resource ID for the banner attribute
         */
        ParsePackageItemArgs(final Package owner, final String[] outError, final int nameRes, final int labelRes, final int iconRes, final int logoRes, final int bannerRes) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Extended argument container for parsing a component (activity, service, provider,
     * receiver) from manifest XML, including process and description resource IDs.
     */
    static class ParseComponentArgs extends ParsePackageItemArgs {
        /** Separate process names defined in the manifest. */
        final String[] sepProcesses;

        /** Resource ID for the process attribute. */
        final int processRes;

        /** Resource ID for the description attribute. */
        final int descriptionRes;

        /** Resource ID for the enabled attribute. */
        final int enabledRes;

        /** Component flags accumulated during parsing. */
        int flags;

        /**
         * Constructs parse arguments for a component.
         *
         * @param owner         the owning {@link Package}
         * @param outError      output array for error messages
         * @param nameRes       resource ID for the name attribute
         * @param labelRes      resource ID for the label attribute
         * @param iconRes       resource ID for the icon attribute
         * @param logoRes       resource ID for the logo attribute
         * @param bannerRes     resource ID for the banner attribute
         * @param sepProcesses  separate process names from the manifest
         * @param processRes    resource ID for the process attribute
         * @param descriptionRes resource ID for the description attribute
         * @param enabledRes    resource ID for the enabled attribute
         */
        ParseComponentArgs(final Package owner, final String[] outError, final int nameRes, final int labelRes, final int iconRes, final int logoRes, final int bannerRes, final String[] sepProcesses, final int processRes, final int descriptionRes, final int enabledRes) {
            super(owner, outError, nameRes, labelRes, iconRes, logoRes, bannerRes);
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Lightweight representation of a package parsed from APK files. Contains only
     * essential metadata without fully parsing the manifest components.
     */
    public static class PackageLite {
        /** The package name declared in the manifest. */
        public final String packageName;

        /** The version code declared in the manifest. */
        public final int versionCode;

        /** The install location preference declared in the manifest. */
        public final int installLocation;

        /** Array of package verifiers declared in the manifest. */
        public final VerifierInfo[] verifiers;

        /** Names of any split APKs, ordered by parsed splitName. */
        public final String[] splitNames;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public final String codePath;

        /** Path of base APK. */
        public final String baseCodePath;

        /** Paths of any split APKs, ordered by parsed splitName. */
        public final String[] splitCodePaths;

        /** Revision code of base APK. */
        public final int baseRevisionCode;

        /** Revision codes of any split APKs, ordered by parsed splitName. */
        public final int[] splitRevisionCodes;

        /** Whether this is a core system application. */
        public final boolean coreApp;

        /** Whether this package supports multiple architectures. */
        public final boolean multiArch;

        /** Whether native libraries should be extracted from the APK. */
        public final boolean extractNativeLibs;

        /**
         * Constructs a new {@link PackageLite} from parsed lightweight data.
         *
         * @param codePath          the path to the package on disk
         * @param baseApk           the parsed {@link ApkLite} for the base APK
         * @param splitNames        names of split APKs
         * @param splitCodePaths    paths of split APKs
         * @param splitRevisionCodes revision codes of split APKs
         */
        public PackageLite(final String codePath, final ApkLite baseApk, final String[] splitNames, final String[] splitCodePaths, final int[] splitRevisionCodes) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns all code paths for this package, including the base APK and all splits.
         *
         * @return a list of all code paths
         */
        public List<String> getAllCodePaths() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Lightweight representation of a single APK file. Contains metadata extracted
     * from the APK without full manifest parsing.
     */
    public static class ApkLite {
        /** The path to this APK file on disk. */
        public final String codePath;

        /** The package name declared in this APK. */
        public final String packageName;

        /** The split name, or {@code null} for the base APK. */
        public final String splitName;

        /** The version code declared in this APK. */
        public final int versionCode;

        /** The revision code for this APK. */
        public final int revisionCode;

        /** The install location preference declared in this APK. */
        public final int installLocation;

        /** Array of package verifiers declared in this APK. */
        public final VerifierInfo[] verifiers;

        /** The signing signatures found in this APK. */
        public final Signature[] signatures;

        /** Whether this is a core system application. */
        public final boolean coreApp;

        /** Whether this APK supports multiple architectures. */
        public final boolean multiArch;

        /** Whether native libraries should be extracted from this APK. */
        public final boolean extractNativeLibs;

        /**
         * Constructs a new {@link ApkLite} with the given parsed metadata.
         *
         * @param codePath         the path to the APK file
         * @param packageName      the package name
         * @param splitName        the split name, or {@code null}
         * @param versionCode      the version code
         * @param revisionCode     the revision code
         * @param installLocation  the install location preference
         * @param verifiers        the list of package verifiers
         * @param signatures       the signing signatures
         * @param coreApp          whether this is a core app
         * @param multiArch        whether multi-architecture is supported
         * @param extractNativeLibs whether native libs should be extracted
         */
        public ApkLite(final String codePath, final String packageName, final String splitName, final int versionCode, final int revisionCode, final int installLocation, final List<VerifierInfo> verifiers, final Signature[] signatures, final boolean coreApp, final boolean multiArch, final boolean extractNativeLibs) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Callback interface for feature checking during package parsing.
     * For SDK_INT 29+.
     */
    public Callback mCallback;

    /**
     * Callback interface that the package parser uses to query system feature
     * availability during manifest processing.
     */
    public interface Callback {
        /**
         * Checks whether the system supports the given feature.
         *
         * @param feature the feature name to check
         * @return {@code true} if the feature is available
         */
        boolean hasFeature(String feature);
    }

    /**
     * Default implementation of {@link Callback} that delegates to
     * {@link PackageManager#hasSystemFeature(String)}.
     */
    public static final class CallbackImpl implements Callback {
        private final PackageManager mPm;

        /**
         * Constructs a {@link CallbackImpl} backed by the given package manager.
         *
         * @param pm the {@link PackageManager} to query for features
         */
        public CallbackImpl(PackageManager pm) {
            mPm = pm;
        }

        /**
         * {@inheritDoc}
         *
         * @param feature the feature name to check
         * @return {@code true} if the system supports the feature
         */
        @Override
        public boolean hasFeature(String feature) {
            return mPm.hasSystemFeature(feature);
        }
    }

    /**
     * Sets the callback used to check feature availability during parsing.
     *
     * @param cb the {@link Callback} implementation to use
     */
    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    /**
     * Constructs a default {@link PackageParser}.
     * For Android 5.0+.
     *
     * @throws RuntimeException always, as this is a stub implementation
     */
    public PackageParser() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Constructs a {@link PackageParser} for the given archive source path.
     *
     * @param archiveSourcePath the file system path to the APK or package directory
     * @throws RuntimeException always, as this is a stub implementation
     */
    public PackageParser(final String archiveSourcePath) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets the list of processes that should be parsed separately. Only components
     * running in these processes will be parsed.
     *
     * @param procs array of process names to parse separately
     */
    public void setSeparateProcesses(final String[] procs) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Configures the parser to only parse core (system) applications.
     *
     * @param onlyCoreApps if {@code true}, only parse core apps
     */
    public void setOnlyCoreApps(final boolean onlyCoreApps) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets the display metrics used for resource resolution during parsing.
     *
     * @param metrics the {@link DisplayMetrics} to use
     */
    public void setDisplayMetrics(final DisplayMetrics metrics) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Checks whether the given file is a valid APK file.
     *
     * @param file the {@link File} to check
     * @return {@code true} if the file is an APK
     */
    public static boolean isApkFile(final File file) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Generates a {@link PackageInfo} from a parsed package.
     *
     * @param p                   the parsed {@link Package}
     * @param gids                the group IDs for the package
     * @param flags               flags controlling which information to include
     * @param firstInstallTime    the time the package was first installed in milliseconds
     * @param lastUpdateTime      the time the package was last updated in milliseconds
     * @param grantedPermissions  the set of permissions granted to the package
     * @param state               the {@link PackageUserState} for the current user
     * @return a {@link PackageInfo} containing the requested information
     */
    public static PackageInfo generatePackageInfo(final PackageParser.Package p, final int[] gids, final int flags, final long firstInstallTime, final long lastUpdateTime, final Set<String> grantedPermissions, final PackageUserState state) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Checks whether a package is available based on its user state.
     *
     * @param state the {@link PackageUserState} to evaluate
     * @return {@code true} if the package is available
     */
    public static boolean isAvailable(final PackageUserState state) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Generates a {@link PackageInfo} from a parsed package for a specific user.
     *
     * @param p                   the parsed {@link Package}
     * @param gids                the group IDs for the package
     * @param flags               flags controlling which information to include
     * @param firstInstallTime    the time the package was first installed in milliseconds
     * @param lastUpdateTime      the time the package was last updated in milliseconds
     * @param grantedPermissions  the set of permissions granted to the package
     * @param state               the {@link PackageUserState} for the target user
     * @param userId              the target user ID
     * @return a {@link PackageInfo} containing the requested information
     */
    public static PackageInfo generatePackageInfo(final PackageParser.Package p, final int[] gids, final int flags, final long firstInstallTime, final long lastUpdateTime, final Set<String> grantedPermissions, final PackageUserState state, final int userId) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parse only lightweight details about the package at the given location.
     * Automatically detects if the package is a monolithic style (single APK
     * file) or cluster style (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     *
     * @param packageFile the file or directory to parse
     * @param flags       optional parse flags
     * @return a {@link PackageLite} containing lightweight package information
     * @throws PackageParserException if parsing fails
     * @see PackageParser#parsePackage(File, int)
     */
    public static PackageLite parsePackageLite(final File packageFile, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parse the package at the given location. Automatically detects if the
     * package is a monolithic style (single APK file) or cluster style
     * (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     *
     * @param packageFile the file or directory to parse
     * @param flags       optional parse flags
     * @return a parsed {@link Package} object
     * @throws PackageParserException if parsing fails
     * @see #parsePackageLite(File, int)
     * @since Android 5.0+
     */
    public Package parsePackage(final File packageFile, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parse the package at the given location using the legacy parsing API.
     *
     * @param sourceFile   the APK source file to parse
     * @param destCodePath the destination code path for the parsed package
     * @param metrics      the {@link DisplayMetrics} for resource resolution
     * @param flags        optional parse flags
     * @return a parsed {@link Package} object, or {@code null} on failure
     * @since Android 2.3+
     */
    public Package parsePackage(final File sourceFile, final String destCodePath, final DisplayMetrics metrics, final int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Collects the manifest digest for the given package.
     *
     * @param pkg the {@link Package} to collect the manifest digest for
     * @throws PackageParserException if digest computation fails
     */
    public void collectManifestDigest(final Package pkg) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Collects the signing certificates for the given package.
     *
     * @param pkg   the {@link Package} to collect certificates for
     * @param flags optional flags controlling certificate collection
     * @throws PackageParserException if certificate collection fails
     */
    public void collectCertificates(final Package pkg, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Utility method that retrieves lightweight details about a single APK
     * file, including package name, split name, and install location.
     *
     * @param apkFile path to a single APK
     * @param flags   optional parse flags, such as
     *            {@link #PARSE_COLLECT_CERTIFICATES}
     * @return an {@link ApkLite} with lightweight APK information
     * @throws PackageParserException if parsing fails
     */
    public static ApkLite parseApkLite(final File apkFile, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Representation of a full package parsed from APK files on disk. A package
     * consists of a single base APK, and zero or more split APKs.
     */
    public final static class Package {
        /** The package name declared in the manifest. */
        public String packageName;

        /** Names of any split APKs, ordered by parsed splitName. */
        public String[] splitNames;

        // TODO: Work towards making these paths invariant.
        /** The volume UUID where this package is installed, or {@code null}. */
        public String volumeUuid;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public String codePath;

        /** Path of base APK. */
        public String baseCodePath;

        /** Paths of any split APKs, ordered by parsed splitName. */
        public String[] splitCodePaths;

        /** Revision code of base APK. */
        public int baseRevisionCode;

        /** Revision codes of any split APKs, ordered by parsed splitName. */
        public int[] splitRevisionCodes;

        /** Flags of any split APKs; ordered by parsed splitName. */
        public int[] splitFlags;

        /**
         * Private flags of any split APKs; ordered by parsed splitName.
         *
         * {@hide}
         */
        public int[] splitPrivateFlags;

        /** Whether the base APK has hardware acceleration enabled. */
        public boolean baseHardwareAccelerated;

        /** The {@link ApplicationInfo} for this package's application. */
        public ApplicationInfo applicationInfo = new ApplicationInfo();

        /** List of permissions declared in the manifest. */
        public final ArrayList<Permission> permissions = new ArrayList<>(0);

        /** List of permission groups declared in the manifest. */
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<>(0);

        /** List of activities declared in the manifest. */
        public final ArrayList<Activity> activities = new ArrayList<>(0);

        /** List of broadcast receivers declared in the manifest. */
        public final ArrayList<Activity> receivers = new ArrayList<>(0);

        /** List of content providers declared in the manifest. */
        public final ArrayList<Provider> providers = new ArrayList<>(0);

        /** List of services declared in the manifest. */
        public final ArrayList<Service> services = new ArrayList<>(0);

        /** List of instrumentation components declared in the manifest. */
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<>(0);

        /** List of permissions requested by this package. */
        public final ArrayList<String> requestedPermissions = new ArrayList<>();

        /** List of protected broadcasts declared by this package. */
        public ArrayList<String> protectedBroadcasts;

        /** List of shared library names used by this package. */
        public ArrayList<String> libraryNames = null;

        /** List of required shared libraries. */
        public ArrayList<String> usesLibraries = null;

        /** List of optional shared libraries. */
        public ArrayList<String> usesOptionalLibraries = null;

        /** Array of shared library file paths available to this package. */
        public String[] usesLibraryFiles = null;

        /** List of preferred activity intent filters. */
        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;

        /** List of original package names this package has been renamed from. */
        public ArrayList<String> mOriginalPackages = null;

        /** The real package name if this package has been renamed. */
        public String mRealPackage = null;

        /** List of permissions adopted from other packages. */
        public ArrayList<String> mAdoptPermissions = null;

        /** Application-level metadata from the manifest. */
        public Bundle mAppMetaData = null;

        /** The version code declared for this package. */
        public int mVersionCode;

        /** The version name declared for this package. */
        public String mVersionName;

        /** The shared user id that this package wants to use. */
        public String mSharedUserId;

        /** The shared user label resource ID. */
        public int mSharedUserLabel;

        /** Signatures that were read from the package. */
        public Signature[] mSignatures;

        /** The signing details including past signing certificates. */
        public SigningDetails mSigningDetails;

        /** The certificate chain arrays for this package's signatures. */
        public Certificate[][] mCertificates;

        /** Preferred order value for quick lookup by the package manager. */
        public int mPreferredOrder = 0;

        // For use by package manager to keep track of when a package was last used.
        /** The last time this package was used, in milliseconds since epoch. */
        public long mLastPackageUsageTimeInMills;

        /** Additional data supplied by callers. */
        public Object mExtras;

        /** Hardware configuration preferences requested by this application. */
        public ArrayList<ConfigurationInfo> configPreferences = null;

        /** Features requested by this application. */
        public ArrayList<FeatureInfo> reqFeatures = null;

        /** Feature groups requested by this application. */
        public ArrayList<FeatureGroupInfo> featureGroups = null;

        /** The preferred install location for this package. */
        public int installLocation;

        /** Whether this is a core system application. */
        public boolean coreApp;

        /** Whether the package is required for all users and cannot be uninstalled. */
        public boolean mRequiredForAllUsers;

        /** The restricted account authenticator type used by this application. */
        public String mRestrictedAccountType;

        /** The required account type without which this application will not function. */
        public String mRequiredAccountType;

        /**
         * Digest suitable for comparing whether this package's manifest is the
         * same as another.
         */
        public ManifestDigest manifestDigest;

        /** The target package name this overlay applies to. */
        public String mOverlayTarget;

        /** The priority of this overlay relative to other overlays. */
        public int mOverlayPriority;

        /** Whether this is a trusted overlay. */
        public boolean mTrustedOverlay;

        /**
         * Data used to feed the KeySetManagerService.
         */
        public ArraySet<PublicKey> mSigningKeys;

        /** Set of key set aliases that can be used to verify upgrades. */
        public ArraySet<String> mUpgradeKeySets;

        /** Mapping from key set aliases to their public keys. */
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;

        /**
         * The install time abi override for this package, if any.
         */
        public String cpuAbiOverride;

        /**
         * Constructs a new {@link Package} with the given package name.
         *
         * @param packageName the package name
         */
        public Package(String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns all code paths for this package, including the base APK and all splits.
         *
         * @return a list of all code paths
         */
        public List<String> getAllCodePaths() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Filtered set of {@link #getAllCodePaths()} that excludes
         * resource-only APKs.
         *
         * @return a list of code paths containing executable code
         */
        public List<String> getAllCodePathsExcludingResourceOnly() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Updates the package name and propagates the change to all child components.
         *
         * @param newName the new package name
         */
        public void setPackageName(final String newName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Checks whether this package contains a component with the given class name.
         *
         * @param name the fully-qualified class name to check
         * @return {@code true} if a component with the given name exists
         */
        public boolean hasComponentClassName(final String name) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Checks whether this package is forward-locked (DRM-protected).
         *
         * @return {@code true} if the package is forward-locked
         */
        public boolean isForwardLocked() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Checks whether this is a system application.
         *
         * @return {@code true} if the package is installed in the system partition
         */
        public boolean isSystemApp() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Checks whether this is a privileged system application.
         *
         * @return {@code true} if the package has the privileged flag
         */
        public boolean isPrivilegedApp() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Checks whether this is an updated system application (a system app that
         * has been updated via the Play Store or other means).
         *
         * @return {@code true} if this is an updated system app
         */
        public boolean isUpdatedSystemApp() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Checks whether this package can have an OAT (ahead-of-time compilation) directory.
         *
         * @return {@code true} if an OAT directory is allowed
         */
        public boolean canHaveOatDir() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this package for debugging.
         *
         * @return a string containing the package name
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Base class for a parsed package component (activity, service, provider, etc.).
     * Contains the owning package reference, intent filters, component class name,
     * and optional metadata.
     *
     * @param <II> the type of {@link IntentInfo} associated with this component
     */
    public static class Component<II extends IntentInfo> {
        /** The owning {@link Package} this component belongs to. */
        public final Package owner;

        /** The list of intent filters associated with this component. */
        public final ArrayList<II> intents;

        /** The fully-qualified class name of this component. */
        public final String className;

        /** Optional metadata bundle for this component. */
        public Bundle metaData;

        /** Cached {@link ComponentName} for this component. */
        ComponentName componentName;

        /** Short name string for debugging. */
        String componentShortName;

        /**
         * Constructs a {@link Component} with only an owner package.
         *
         * @param owner the owning {@link Package}
         */
        public Component(final Package owner) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Constructs a {@link Component} from parse arguments and populates a
         * {@link PackageItemInfo}.
         *
         * @param args     the {@link ParsePackageItemArgs} with parse context
         * @param outInfo  the {@link PackageItemInfo} to populate
         */
        public Component(final ParsePackageItemArgs args, final PackageItemInfo outInfo) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Constructs a {@link Component} from component parse arguments and populates
         * a {@link ComponentInfo}.
         *
         * @param args     the {@link ParseComponentArgs} with parse context
         * @param outInfo  the {@link ComponentInfo} to populate
         */
        public Component(final ParseComponentArgs args, final ComponentInfo outInfo) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Copy constructor that creates a clone of the given component.
         *
         * @param clone the {@link Component} to clone
         */
        public Component(final Component<II> clone) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns the {@link ComponentName} for this component.
         *
         * @return the {@link ComponentName}, creating it if necessary
         */
        public ComponentName getComponentName() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Appends the short name (package/class) of this component to the given
         * {@link StringBuilder}.
         *
         * @param sb the {@link StringBuilder} to append to
         */
        public void appendComponentShortName(final StringBuilder sb) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Prints the short name (package/class) of this component to the given
         * {@link PrintWriter}.
         *
         * @param pw the {@link PrintWriter} to print to
         */
        public void printComponentShortName(final PrintWriter pw) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Updates the package name for this component and its associated data.
         *
         * @param packageName the new package name
         */
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Parsed representation of a {@code <permission>} element from an AndroidManifest.
     */
    public final static class Permission extends Component<IntentInfo> {
        /** The {@link PermissionInfo} metadata for this permission. */
        public final PermissionInfo info;

        /** Whether this permission is a permission tree root. */
        public boolean tree;

        /** The {@link PermissionGroup} this permission belongs to, or {@code null}. */
        public PermissionGroup group;

        /**
         * Constructs a {@link Permission} with only an owner package.
         *
         * @param owner the owning {@link Package}
         */
        public Permission(final Package owner) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        /**
         * Constructs a {@link Permission} with an owner package and permission info.
         *
         * @param owner the owning {@link Package}
         * @param info  the {@link PermissionInfo} for this permission
         */
        public Permission(final Package owner, final PermissionInfo info) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        /**
         * {@inheritDoc}
         *
         * @param packageName the new package name
         */
        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this permission.
         *
         * @return a string containing the permission name
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Parsed representation of a {@code <permission-group>} element from an AndroidManifest.
     */
    public final static class PermissionGroup extends Component<IntentInfo> {
        /** The {@link PermissionGroupInfo} metadata for this group. */
        public final PermissionGroupInfo info;

        /**
         * Constructs a {@link PermissionGroup} with only an owner package.
         *
         * @param owner the owning {@link Package}
         */
        public PermissionGroup(final Package owner) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        /**
         * Constructs a {@link PermissionGroup} with an owner package and info.
         *
         * @param owner the owning {@link Package}
         * @param info  the {@link PermissionGroupInfo} for this group
         */
        public PermissionGroup(final Package owner, final PermissionGroupInfo info) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        /**
         * {@inheritDoc}
         *
         * @param packageName the new package name
         */
        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this permission group.
         *
         * @return a string containing the permission group name
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Parsed representation of an {@code <activity>} element from an AndroidManifest.
     */
    public final static class Activity extends Component<ActivityIntentInfo> {
        /** The {@link ActivityInfo} metadata for this activity. */
        public final ActivityInfo info;

        /**
         * Constructs an {@link Activity} from parse arguments.
         *
         * @param args the {@link ParseComponentArgs} with parse context
         * @param info the {@link ActivityInfo} to populate
         */
        public Activity(final ParseComponentArgs args, final ActivityInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        /**
         * {@inheritDoc}
         *
         * @param packageName the new package name
         */
        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this activity.
         *
         * @return a string containing the activity class name
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Parsed representation of a {@code <service>} element from an AndroidManifest.
     */
    public final static class Service extends Component<ServiceIntentInfo> {
        /** The {@link ServiceInfo} metadata for this service. */
        public final ServiceInfo info;

        /**
         * Constructs a {@link Service} from parse arguments.
         *
         * @param args the {@link ParseComponentArgs} with parse context
         * @param info the {@link ServiceInfo} to populate
         */
        public Service(final ParseComponentArgs args, final ServiceInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        /**
         * {@inheritDoc}
         *
         * @param packageName the new package name
         */
        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this service.
         *
         * @return a string containing the service class name
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Parsed representation of a {@code <provider>} element from an AndroidManifest.
     */
    public final static class Provider extends Component<ProviderIntentInfo> {
        /** The {@link ProviderInfo} metadata for this provider. */
        public final ProviderInfo info;

        /** Whether this content provider is syncable. */
        public boolean syncable;

        /**
         * Constructs a {@link Provider} from parse arguments.
         *
         * @param args the {@link ParseComponentArgs} with parse context
         * @param info the {@link ProviderInfo} to populate
         */
        public Provider(final ParseComponentArgs args, final ProviderInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        /**
         * Copy constructor that creates a clone of an existing provider.
         *
         * @param existingProvider the {@link Provider} to clone
         */
        public Provider(final Provider existingProvider) {
            super(existingProvider);
            throw new RuntimeException("Stub!");
        }

        /**
         * {@inheritDoc}
         *
         * @param packageName the new package name
         */
        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this provider.
         *
         * @return a string containing the provider authority
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Parsed representation of an {@code <instrumentation>} element from an AndroidManifest.
     */
    public final static class Instrumentation extends Component<IntentInfo> {
        /** The {@link InstrumentationInfo} metadata for this instrumentation. */
        public final InstrumentationInfo info;

        /**
         * Constructs an {@link Instrumentation} from parse arguments.
         *
         * @param args the {@link ParsePackageItemArgs} with parse context
         * @param info the {@link InstrumentationInfo} to populate
         */
        public Instrumentation(final ParsePackageItemArgs args, final InstrumentationInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        /**
         * {@inheritDoc}
         *
         * @param packageName the new package name
         */
        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this instrumentation.
         *
         * @return a string containing the instrumentation class name
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Extended {@link IntentFilter} that includes additional UI-related metadata
     * such as labels, icons, and preference values for intent resolution.
     */
    @SuppressLint("ParcelCreator")
    public static class IntentInfo extends IntentFilter {
        /** Whether this intent filter has a default match. */
        public boolean hasDefault;

        /** Resource ID for the label. */
        public int labelRes;

        /** Non-localized label text. */
        public CharSequence nonLocalizedLabel;

        /** Resource ID for the icon. */
        public int icon;

        /** Resource ID for the logo. */
        public int logo;

        /** Resource ID for the banner. */
        public int banner;

        /** Preferred order for this intent filter. */
        public int preferred;
    }

    /**
     * Intent filter associated with an activity component.
     */
    @SuppressLint("ParcelCreator")
    public final static class ActivityIntentInfo extends IntentInfo {
        /** The {@link Activity} this intent filter belongs to. */
        public final Activity activity;

        /**
         * Constructs an {@link ActivityIntentInfo} for the given activity.
         *
         * @param activity the owning {@link Activity}
         */
        public ActivityIntentInfo(final Activity activity) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this intent info.
         *
         * @return a string containing the intent filter details
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Intent filter associated with a service component.
     */
    @SuppressLint("ParcelCreator")
    public final static class ServiceIntentInfo extends IntentInfo {
        /** The {@link Service} this intent filter belongs to. */
        public final Service service;

        /**
         * Constructs a {@link ServiceIntentInfo} for the given service.
         *
         * @param service the owning {@link Service}
         */
        public ServiceIntentInfo(final Service service) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this intent info.
         *
         * @return a string containing the intent filter details
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Intent filter associated with a content provider component.
     */
    @SuppressLint("ParcelCreator")
    public static final class ProviderIntentInfo extends IntentInfo {
        /** The {@link Provider} this intent filter belongs to. */
        public final Provider provider;

        /**
         * Constructs a {@link ProviderIntentInfo} for the given provider.
         *
         * @param provider the owning {@link Provider}
         */
        public ProviderIntentInfo(final Provider provider) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Returns a string representation of this intent info.
         *
         * @return a string containing the intent filter details
         */
        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Exception thrown when package parsing fails. Contains an error code
     * indicating the type of failure.
     */
    public static class PackageParserException extends Exception {
        /**
         * Constructs a {@link PackageParserException} with an error code and message.
         *
         * @param error         the error code identifying the type of failure
         * @param detailMessage the human-readable error message
         */
        public PackageParserException(int error, String detailMessage) {
            super(detailMessage);
            throw new RuntimeException("Stub!");
        }

        /**
         * Constructs a {@link PackageParserException} with an error code, message, and cause.
         *
         * @param error         the error code identifying the type of failure
         * @param detailMessage the human-readable error message
         * @param throwable     the underlying cause of the exception
         */
        public PackageParserException(int error, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Represents the signing details of a package, including current and past
     * signing certificates.
     */
    public static class SigningDetails {
        /** Sentinel value representing unknown signing details. */
        public static final SigningDetails UNKNOWN = null;

        /** The current signing signatures. */
        public Signature[] signatures;

        /** Past signing certificates used in rotation. */
        public Signature[] pastSigningCertificates;
    }
}
