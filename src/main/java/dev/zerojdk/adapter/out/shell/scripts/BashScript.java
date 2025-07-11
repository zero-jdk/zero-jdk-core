package dev.zerojdk.adapter.out.shell.scripts;

import dev.zerojdk.domain.service.shell.ShellScript;
import lombok.Data;

@Data
public final class BashScript implements ShellScript {
    private final String name = "zjdk.plugin.bash";
    private final String content = """
        _clean_path_of_jdk_bin() {
          local IFS=: newpath= part
          for part in $PATH; do
            if [[ $part != */.zjdk/*/bin ]]; then
              newpath+="$part:"
            fi
          done
          printf "%s\\n" "${newpath%:}"
        }
        
        _read_property() {
          local file="$1"
          local key="$2"
        
          [[ -r "$file" ]] || { printf "" ; return 1; }
        
          local line
          while IFS= read -r line || [[ -n $line ]]; do
            # Trim leading/trailing whitespace
            line="${line#"${line%%[![:space:]]*}"}"
            line="${line%"${line##*[![:space:]]}"}"
        
            [[ -z "$line" || "$line" =~ ^[#\\\\!] ]] && continue
        
            local k="${line%%=*}"
            local v="${line#*=}"
            k="${k//[[:space:]]/}"
            if [[ "$k" == "$key" ]]; then
              v="${v//[[:space:]]/}"
              printf "%s\\n" "$v"
              return 0
            fi
          done < "$file"
        }
        
        _find_version_in_tree() {
          local dir="${1:-$PWD}"
          local version
        
          while [[ "$dir" != "/" && -n "$dir" ]]; do
            version=$(_read_property "$dir/.zjdk/config.properties" version)
            [[ -n "$version" ]] && { printf "%s\\n" "$version"; return; }
        
            dir="$(dirname "$dir")"
          done
        }
        
        _default_version() {
          _read_property "$HOME/.zjdk/config.properties" version
        }
        
        _java_home_for_version() {
          local version="$1"
          _read_property "$HOME/.zjdk/releases/$version/.info" home
        }
        
        _apply_java_home() {
          local java_home="$1"
          if [[ -n "$java_home" ]]; then
            export JAVA_HOME="$java_home"
            export PATH="$JAVA_HOME/bin:$(_clean_path_of_jdk_bin)"
          else
            unset JAVA_HOME
            export PATH="$(_clean_path_of_jdk_bin)"
          fi
        }
        
        set_java_home_from_jdk_folder() {
          local version
          version=$(_find_version_in_tree "$PWD")
          [[ -z "$version" ]] && version=$(_default_version)
        
          local java_home
          [[ -n "$version" ]] && java_home=$(_java_home_for_version "$version")
        
          _apply_java_home "$java_home"
        }
        
        # Hook into PROMPT_COMMAND
        __zjdk_hook() {
          set_java_home_from_jdk_folder
        }
        
        PROMPT_COMMAND="__zjdk_hook${PROMPT_COMMAND:+; $PROMPT_COMMAND}"
        # Initial run
        __zjdk_hook
        """;
}
