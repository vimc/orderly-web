import {mount} from "@vue/test-utils";
import Vue from "vue";
import ChangeLog from "../../../js/components/runReport/changeLog.vue";

describe(`changeLog`, () => {
    const changelogTypeOptions = ["internal", "public"]

    const getWrapper = (propsData: any = {}) => {
        return mount(ChangeLog,
            {
                propsData: {
                    changelogTypeOptions: changelogTypeOptions,
                    customStyle: {label: "col-sm-2 text-right", control: "col-sm-6"},
                    ...propsData,
                }
            })
    }

    it(`renders component as expected`, () => {
        const wrapper = getWrapper();
        expect(wrapper.find("#changelog-message").exists()).toBeTruthy()
        expect(wrapper.find("#changelog-type").exists()).toBeTruthy()
        expect(wrapper.vm.$data).toEqual({"changeLogMessageValue": "", "changeLogTypeValue": "internal"})
        expect(wrapper.vm.$props).toEqual(
            {
                changelogTypeOptions,
                customStyle: {label: "col-sm-2 text-right", control: "col-sm-6"}
            })
    })

    it(`emits changelogType first index when component gets mounted`, () => {
        const wrapper = getWrapper();
        expect(wrapper.emitted().changelogType.length).toBe(1)
        expect(wrapper.emitted().changelogType[0][0]).toBe("internal")
    })

    it(`can set and get changelog message`, () => {
        const wrapper = getWrapper();
        const message = wrapper.find("#changelogMessage")
        message.setValue("new message")
        expect(wrapper.vm.$data.changeLogMessageValue).toBe("new message")

        expect(wrapper.emitted().changelogMessage.length).toBe(1)
        expect(wrapper.emitted().changelogMessage[0][0]).toEqual("new message")

        const messageValue = message.element as HTMLTextAreaElement
        expect(messageValue.value).toBe("new message")
    })

    it(`can select and get changelog type`, () => {
        const wrapper = getWrapper();
        const options = wrapper.find("#changelogType")
            .find("select").findAll("option")
        options.at(1).setSelected()

        expect(wrapper.emitted().changelogType.length).toBe(2)
        expect(wrapper.emitted().changelogType[0][0]).toEqual(changelogTypeOptions[0]) //on mount initial selection
        expect(wrapper.emitted().changelogType[1][0]).toEqual(changelogTypeOptions[1])  // selected value

        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public")
        const typeValue = wrapper.find("#changelogType").element as HTMLSelectElement
        expect(typeValue.value).toBe("public")
    })

    it(`renders changelog col styles correctly if custom-style prop is set`, () => {
        const wrapper = getWrapper();

        const label = ["col-form-label", "col-sm-2", "text-right"]
        const control = ["col-sm-6"]

        const changelogMessage = wrapper.find(ChangeLog).find("#changelog-message")
        const changelogType= wrapper.find(ChangeLog).find("#changelog-type")

        expect(changelogMessage.find("label").classes()).toEqual(label)
        expect(changelogMessage.find("#change-message-control").classes()).toEqual(control)
        expect(changelogType.find("label").classes()).toEqual(label)
        expect(changelogType.find("#change-type-control").classes()).toEqual(control)
    });

    it(`can set changelog message from initial value`, async () => {
        const wrapper = getWrapper({initialMessage: "some message"});
        expect(wrapper.vm.$data.changeLogMessageValue).toBe("some message");
        await Vue.nextTick();
        expect((wrapper.find("#changelogMessage").element as HTMLTextAreaElement).value).toBe("some message");
    });

    it(`can set changelog message from initial type`, async () => {
        const wrapper = getWrapper({initialType: "public"});
        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public");
        await Vue.nextTick();
        expect((wrapper.find("#changelogType").element as HTMLSelectElement).value).toBe("public");
        expect(wrapper.emitted().changelogType).toBeUndefined();
    });
})
