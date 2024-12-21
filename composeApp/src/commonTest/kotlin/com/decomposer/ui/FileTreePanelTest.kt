package com.decomposer.ui

import com.decomposer.runtime.connection.model.ProjectSnapshot
import org.junit.Test

class FileTreePanelTest {
    @Test
    fun testFileTreeMapping() {
        val projectSnapshot = ProjectSnapshot(
            fileTree = setOf(
                "/home/jim/com/example/A.kt",
                "/home/jim/com/example/B.kt",
                "/home/jim/android/C.kt",
                "/home/jim/android/D.kt"
            ),
            packagesByPath = emptyMap()
        )
        val fileTree = projectSnapshot.buildFileTree {  }
        assert(fileTree.root.level == 0)
        assert(fileTree.root.name == "/home/jim")
        assert(fileTree.root.children.size == 2)
    }
}
