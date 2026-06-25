document.addEventListener("DOMContentLoaded", function () {
    const sidebar = document.getElementById("sidebar");
    const mobileMenuBtn = document.getElementById("mobileMenuBtn");
    const sidebarToggle = document.getElementById("sidebarToggle");
    const passwordInput = document.getElementById("password");
    const passwordToggle = document.getElementById("passwordToggle");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const confirmPasswordToggle = document.getElementById("confirmPasswordToggle");
    const accountType = document.getElementById("accountType");
    const studentFields = document.getElementById("studentFields");
    const lecturerFields = document.getElementById("lecturerFields");
    const studentInputs = studentFields ? studentFields.querySelectorAll("input") : [];
    const lecturerInputs = lecturerFields ? lecturerFields.querySelectorAll("input") : [];

    if (sidebar) {
        const sidebarLinks = Array.from(sidebar.querySelectorAll(".sidebar-menu a"));
        const currentPath = window.location.pathname.replace(/\/$/, "") || "/";

        const normalizePath = function (path) {
            return path.replace(/\/$/, "") || "/";
        };

        const isActivePath = function (linkPath) {
            const normalizedLinkPath = normalizePath(linkPath);

            if (normalizedLinkPath === "/dashboard") {
                return currentPath === normalizedLinkPath;
            }

            return currentPath === normalizedLinkPath || currentPath.startsWith(normalizedLinkPath + "/");
        };

        const setActiveLink = function (activeLink) {
            sidebarLinks.forEach(function (link) {
                const isActive = link === activeLink;
                link.classList.toggle("active", isActive);
                link.toggleAttribute("aria-current", isActive);
            });
        };

        const matchedLink = sidebarLinks
            .filter(function (link) {
                return isActivePath(new URL(link.href).pathname);
            })
            .sort(function (firstLink, secondLink) {
                return new URL(secondLink.href).pathname.length - new URL(firstLink.href).pathname.length;
            })[0];
        const defaultLink = sidebar.querySelector('.sidebar-menu a[href$="/dashboard"], .sidebar-menu a[href="/dashboard"]');

        setActiveLink(matchedLink || defaultLink || sidebarLinks[0]);

        sidebarLinks.forEach(function (link) {
            link.addEventListener("click", function () {
                setActiveLink(link);
            });
        });
    }

    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener("click", function () {
            sidebar.classList.toggle("open");
        });
    }

    if (sidebarToggle) {
        sidebarToggle.addEventListener("click", function () {
            sidebar.classList.remove("open");
        });
    }

    if (passwordInput && passwordToggle) {
        passwordToggle.addEventListener("click", function () {
            const isHidden = passwordInput.type === "password";
            passwordInput.type = isHidden ? "text" : "password";
            passwordToggle.textContent = isHidden ? "Hide" : "Show";
            passwordToggle.setAttribute("aria-label", isHidden ? "Hide password" : "Show password");
        });
    }

    if (confirmPasswordInput && confirmPasswordToggle) {
        confirmPasswordToggle.addEventListener("click", function () {
            const isHidden = confirmPasswordInput.type === "password";
            confirmPasswordInput.type = isHidden ? "text" : "password";
            confirmPasswordToggle.textContent = isHidden ? "Hide" : "Show";
            confirmPasswordToggle.setAttribute("aria-label", isHidden ? "Hide password confirmation" : "Show password confirmation");
        });
    }

    if (accountType && studentFields && lecturerFields) {
        const syncAccountType = function () {
            const isStudent = accountType.value === "SV";
            studentFields.classList.toggle("d-none", !isStudent);
            lecturerFields.classList.toggle("d-none", isStudent);

            studentInputs.forEach(function (input) {
                input.required = isStudent;
            });
            lecturerInputs.forEach(function (input) {
                input.required = !isStudent;
            });
        };

        accountType.addEventListener("change", syncAccountType);
        syncAccountType();
    }
});
