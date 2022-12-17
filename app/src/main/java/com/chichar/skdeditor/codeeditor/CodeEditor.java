package com.chichar.skdeditor.codeeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditor extends ListView {

    private CodeEditorAdapter codeEditorAdapter;

    private int currentMatchedIndex = -1;

    private final ArrayList<Integer> results = new ArrayList<>();

    public CodeEditor(Context context) {
        super(context);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getText() {
        if (codeEditorAdapter == null) {
            return "";
        }
        return codeEditorAdapter.getText();
    }

    public void setText(String text) {
        String[] strings = text.split("\n");
        if (codeEditorAdapter == null) {
            codeEditorAdapter = new CodeEditorAdapter(getContext(), strings);
            setAdapter(codeEditorAdapter);
            return;
        }
        codeEditorAdapter.setText(strings);
    }

    public ArrayList<Integer> findMatches(Pattern pattern) {
        results.clear();
        if (pattern.toString().isEmpty() || codeEditorAdapter == null) return results;

        Matcher matcher = pattern.matcher(codeEditorAdapter.getText());
        ArrayList<Integer[]> matchedIndexes = new ArrayList<>();
        while (matcher.find()) matchedIndexes.add(new Integer[]{matcher.start(), matcher.end()});
        if (matchedIndexes.isEmpty()) {
            findNextMatch();
        }
        String[] textArr = codeEditorAdapter.getTextArray();

        int globalIndex = 0;
        int resultIndex = 0;
        ArrayList<Integer[]> highlight = new ArrayList<>();
        if (matchedIndexes.isEmpty()) {
            return results;
        }
        for (int i = 0; i < textArr.length; i++) { //index, start, end
            for (int localIndex = 0; localIndex < textArr[i].length(); localIndex++) {

                if (matchedIndexes.get(resultIndex)[1] == globalIndex) {
                    if (highlight.size() == 0) {
                        highlight.add(new Integer[]{i, 0, localIndex});
                    }
                    Integer[] currHighlight = highlight.get(highlight.size() - 1);
                    if (currHighlight[1] == null) {
                        currHighlight[1] = 0;
                    }
                    if (localIndex > currHighlight[1] && currHighlight[0] == i) {
                        currHighlight[2] = localIndex;
                    }
                    if (currHighlight[0] != i) {
                        highlight.add(currHighlight);
                        for (int skippedIndex = currHighlight[0] + 1; skippedIndex < i; skippedIndex++) {
                            highlight.add(new Integer[]{skippedIndex, 0, textArr[skippedIndex].length()});
                        }
                        highlight.add(new Integer[]{i, 0, localIndex});
                    }
                    else {
                        highlight.remove(highlight.size() - 1);
                        highlight.add(currHighlight);
                    }
                    resultIndex++;
                    if (resultIndex == matchedIndexes.size()) {
                        break;
                    }
                }
                if (matchedIndexes.get(resultIndex)[0] == globalIndex) {
                    highlight.add(new Integer[]{i, localIndex, textArr[i].length()});
                    Integer[] currHighlight = highlight.get(highlight.size() - 1);
                    if (currHighlight[0] == null) {
                        currHighlight[0] = textArr[i].length() - 1;
                    }
                    currHighlight[1] = localIndex;
                    highlight.remove(highlight.size() - 1);
                    highlight.add(currHighlight);
                    results.add(i);
                }
                globalIndex++;
            }
            globalIndex++;
            if (resultIndex == matchedIndexes.size()) {
                break;
            }
        }
        codeEditorAdapter.setHighlight(highlight);
        findNextMatch();
        return results;
    }

    public void findNextMatch() {
        if (results.isEmpty()) {
            Toast.makeText(getContext(), "Nothing found", Toast.LENGTH_SHORT).show();
            return;
        }
        currentMatchedIndex++;
        if (currentMatchedIndex >= results.size()) {
            currentMatchedIndex = -1;
            Toast.makeText(getContext(), "Reached end of the document", Toast.LENGTH_SHORT).show();
            findNextMatch();
            return;
        }

        Integer currentMatch = results.get(currentMatchedIndex);
        super.setSelection(currentMatch);
    }

    public void findPrevMatch() {
        if (results.isEmpty()) {
            Toast.makeText(getContext(), "Nothing found", Toast.LENGTH_SHORT).show();
            return;
        }
        currentMatchedIndex--;
        if (currentMatchedIndex < 0) {
            Toast.makeText(getContext(), "Reached start of the document", Toast.LENGTH_SHORT).show();
            currentMatchedIndex = results.size() - 1;
        }
        Integer currentMatch = results.get(currentMatchedIndex);
        super.setSelection(currentMatch);
    }

    public void clearMatches() {
        currentMatchedIndex = -1;
        results.clear();
        codeEditorAdapter.clearHighlight();
    }

    public boolean replaceAllMatches(Pattern pattern, String replacement) {
        return codeEditorAdapter.replaceAll(pattern, replacement);
    }

    public void removeMatch(Integer index) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).equals(index)) {
                results.remove(i);
                break;
            }
        }
    }
}