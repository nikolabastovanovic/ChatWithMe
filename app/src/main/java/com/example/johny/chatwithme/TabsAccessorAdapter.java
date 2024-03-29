package com.example.johny.chatwithme;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.johny.chatwithme.ChatFragment;
import com.example.johny.chatwithme.ContactsFragment;
import com.example.johny.chatwithme.GroupFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter
{

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

//            case 1:
//                GroupFragment groupFragment = new GroupFragment();
//                return groupFragment;

            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 2:
                RequestFragment requestFragmtn = new RequestFragment();
                return requestFragmtn;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chat";

//            case 1:
//                return "Group";

            case 1:
                return "Contacts";

            case 2:
                return "Requests";

            default:
                return null;
        }
    }
}
